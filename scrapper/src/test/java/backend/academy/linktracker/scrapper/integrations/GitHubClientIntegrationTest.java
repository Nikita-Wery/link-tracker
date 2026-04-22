package backend.academy.linktracker.scrapper.integrations;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.linktracker.scrapper.client.external.GitHubClient;
import backend.academy.linktracker.scrapper.client.responsehandler.APIBadResponseHandler;
import backend.academy.linktracker.scrapper.config.scrapperconfiguration.api.rest.RestExternalClientsConfiguration;
import backend.academy.linktracker.scrapper.dto.github.GithubIssueResponse;
import backend.academy.linktracker.scrapper.dto.github.GithubRepositoryUpdateTime;
import backend.academy.linktracker.scrapper.exception.externalexception.ExternalApiException;
import backend.academy.linktracker.scrapper.properties.GithubProperties;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest
class GitHubClientIntegrationTest {

    private GitHubClient gitHubClient;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmRuntimeInfo) {

        GithubProperties properties = new GithubProperties();
        properties.setHost(wmRuntimeInfo.getHttpBaseUrl());
        properties.setToken("test-token");

        APIBadResponseHandler handler = new APIBadResponseHandler();

        RestExternalClientsConfiguration config = new RestExternalClientsConfiguration();

        gitHubClient = config.gitHubClient(handler, properties);
    }

    @Test
    void shouldReturnIssuesSuccessfully() {

        String body = "GHub".repeat(50);

        stubFor(get(urlEqualTo("/repos/test/repo/issues")).willReturn(okJson("""
                [
                  {
                    "id": 1984723912,
                    "node_id": "I_kwDOExampleIssue",
                    "html_url": "https://github.com/spring-projects/spring-boot/issues/45678",
                    "number": 45678,
                    "title": "NullPointerException when using RestClient with custom interceptor",
                    "user": {
                      "login": "octocat",
                      "id": 583231,
                      "html_url": "https://github.com/octocat"
                    },
                    "state": "open",
                    "comments": 3,
                    "created_at": "2026-04-20T09:15:32Z",
                    "updated_at": "2026-04-21T14:48:11Z",
                    "body": "%s",
                    "pull_request": null
                  }
                ]
                """.formatted(body))));

        List<GithubIssueResponse> response = gitHubClient.getRepositoryIssueUpdate("test", "repo");

        assertEquals(1, response.size());

        GithubIssueResponse issue = response.getFirst();

        assertEquals("NullPointerException when using RestClient with custom interceptor", issue.title());

        assertEquals(body, issue.body());

        assertEquals("https://github.com/spring-projects/spring-boot/issues/45678", issue.url());

        assertEquals("octocat", issue.user().login());

        assertEquals(OffsetDateTime.parse("2026-04-20T09:15:32Z"), issue.createdAt());

        assertEquals(OffsetDateTime.parse("2026-04-21T14:48:11Z"), issue.updatedAt());

        assertNull(issue.pullRequest());

        verify(getRequestedFor(urlEqualTo("/repos/test/repo/issues")));
    }

    @Test
    void shouldReturnEmptyIssueList() {

        stubFor(get(urlEqualTo("/repos/test/repo/issues")).willReturn(okJson("[]")));

        List<GithubIssueResponse> response = gitHubClient.getRepositoryIssueUpdate("test", "repo");

        assertTrue(response.isEmpty());

        verify(getRequestedFor(urlEqualTo("/repos/test/repo/issues")));
    }

    @Test
    void shouldReturnRepositoryUpdateTimeSuccessfully() {

        stubFor(get(urlEqualTo("/repos/test/repo")).willReturn(okJson("""
                {
                  "id": 123456,
                  "name": "repo",
                  "full_name": "test/repo",
                  "private": false,
                  "html_url": "https://github.com/test/repo",
                  "description": "Test repository",
                  "fork": false,
                  "created_at": "2025-01-10T12:00:00Z",
                  "updated_at": "2026-04-21T15:30:00Z",
                  "pushed_at": "2026-04-21T16:45:00Z"
                }
                """)));

        GithubRepositoryUpdateTime response = gitHubClient.getRepositoryUpdateTime("test", "repo");

        assertEquals(OffsetDateTime.parse("2026-04-21T15:30:00Z"), response.updateAt());

        verify(getRequestedFor(urlEqualTo("/repos/test/repo")));
    }

    @Test
    void shouldThrowGithubApiException_whenGithubReturns403() {

        stubFor(get(urlEqualTo("/repos/test/repo/issues"))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                        {
                          "message": "API rate limit exceeded for 192.168.1.1.",
                          "documentation_url": "https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting"
                        }
                        """)));

        ExternalApiException exception =
                assertThrows(ExternalApiException.class, () -> gitHubClient.getRepositoryIssueUpdate("test", "repo"));

        assertEquals(403, exception.getStatusCode());

        assertTrue(exception.getBody().contains("API rate limit exceeded"));

        verify(getRequestedFor(urlEqualTo("/repos/test/repo/issues")));
    }

    @Test
    void shouldThrowGithubApiException_whenGithubReturns404() {

        stubFor(get(urlEqualTo("/repos/test/repo"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                        {
                          "message": "Not Found",
                          "documentation_url": "https://docs.github.com/rest/repos/repos#get-a-repository"
                        }
                        """)));

        ExternalApiException exception =
                assertThrows(ExternalApiException.class, () -> gitHubClient.getRepositoryUpdateTime("test", "repo"));

        assertEquals(404, exception.getStatusCode());

        assertTrue(exception.getBody().contains("Not Found"));

        verify(getRequestedFor(urlEqualTo("/repos/test/repo")));
    }

    @Test
    void shouldSendAuthorizationHeader() {

        stubFor(get(urlEqualTo("/repos/test/repo/issues")).willReturn(okJson("[]")));

        gitHubClient.getRepositoryIssueUpdate("test", "repo");

        verify(getRequestedFor(urlEqualTo("/repos/test/repo/issues"))
                .withHeader("Authorization", equalTo("Bearertest-token")));
    }

    @Test
    void shouldHandlePullRequestIssueCorrectly() {

        stubFor(get(urlEqualTo("/repos/test/repo/issues")).willReturn(okJson("""
                [
                  {
                    "title": "Add support for WireMock",
                    "body": "Implemented WireMock integration tests",
                    "html_url": "https://github.com/test/repo/pull/42",
                    "created_at": "2026-04-20T09:15:32Z",
                    "updated_at": "2026-04-21T14:48:11Z",
                    "user": {
                      "login": "developer"
                    },
                    "pull_request": {
                      "url": "https://api.github.com/repos/test/repo/pulls/42"
                    }
                  }
                ]
                """)));

        List<GithubIssueResponse> response = gitHubClient.getRepositoryIssueUpdate("test", "repo");

        assertEquals(1, response.size());

        GithubIssueResponse issue = response.getFirst();

        assertNotNull(issue.pullRequest());

        assertEquals("developer", issue.user().login());

        verify(getRequestedFor(urlEqualTo("/repos/test/repo/issues")));
    }
}
