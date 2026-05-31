package backend.academy.linktracker.scrapper.integrations;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.linktracker.scrapper.client.external.StackOverflowClient;
import backend.academy.linktracker.scrapper.client.responsehandler.APIBadResponseHandler;
import backend.academy.linktracker.scrapper.config.scrapperconfiguration.api.rest.RestExternalClientsConfiguration;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowAnswerResponse;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowCommentResponse;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowWrapper;
import backend.academy.linktracker.scrapper.exception.externalexception.ExternalApiClientException;
import backend.academy.linktracker.scrapper.exception.externalexception.ExternalApiException;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import backend.academy.linktracker.scrapper.service.logs.ScrapperMetricsService;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("integration")
@WireMockTest
@ExtendWith(MockitoExtension.class)
class StackOverflowIntegrationTest {

    private StackOverflowClient stackOverflowClient;

    @Mock
    private ScrapperMetricsService scrapperMetricsService;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmRuntimeInfo) {

        StackoverflowProperties properties = new StackoverflowProperties();
        properties.setHost(wmRuntimeInfo.getHttpBaseUrl());

        properties.setKey("test-key");
        properties.setAccessToken("test-token");

        APIBadResponseHandler handler = new APIBadResponseHandler(scrapperMetricsService);

        RestExternalClientsConfiguration config = new RestExternalClientsConfiguration();

        stackOverflowClient = config.stackOverflowClient(handler, properties);
    }

    @Test
    void shouldThrowExternalApiException_whenQuestionReturns403() {

        stubFor(get(urlPathEqualTo("/questions/123"))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withHeader("Content-Type", "application/json")
                        .withBody("quota exceeded")));

        ExternalApiException ex = assertThrows(
                ExternalApiClientException.class,
                () -> stackOverflowClient.getQuestionUpdateTime(123L, "stackoverflow", "test-key", "test-token"));

        assertEquals(403, ex.getStatusCode());
        assertEquals("quota exceeded", ex.getBody());

        verify(getRequestedFor(urlPathEqualTo("/questions/123")));
    }

    @Test
    void shouldReturnAnswerUpdatesSuccessfully() {

        stubFor(get(urlPathEqualTo("/questions/123/answers"))
                .withQueryParam("site", equalTo("stackoverflow"))
                .willReturn(okJson("""
                {
                  "items": [
                    {
                      "answer_id": 999,
                      "body": "Some answer",
                      "creation_date": 1774346400,
                      "last_activity_date": 1774346410,
                      "is_accepted": true,
                      "owner": {
                        "display_name": "answer-user"
                      }
                    }
                  ]
                }
                """)));

        StackOverflowWrapper<StackOverflowAnswerResponse> response =
                stackOverflowClient.getAnswerUpdates(123L, "stackoverflow");

        assertNotNull(response);
        assertEquals(1, response.items().size());
        assertEquals(999L, response.items().get(0).id());

        verify(getRequestedFor(urlPathEqualTo("/questions/123/answers")));
    }

    @Test
    void shouldReturnCommentUpdatesSuccessfully() {

        stubFor(get(urlPathEqualTo("/questions/123/comments"))
                .withQueryParam("site", equalTo("stackoverflow"))
                .willReturn(okJson("""
                {
                  "items": [
                    {
                      "comment_id": 777,
                      "post_id": 123,
                      "creation_date": 1713600000,
                      "body": "Nice question!"
                    }
                  ]
                }
                """)));

        StackOverflowWrapper<StackOverflowCommentResponse> response =
                stackOverflowClient.getCommentUpdates(123L, "stackoverflow");

        assertNotNull(response);
        assertEquals(1, response.items().size());
        assertEquals(777, response.items().get(0).id());

        verify(getRequestedFor(urlPathEqualTo("/questions/123/comments")));
    }

    @Test
    void shouldThrowExternalApiException_whenCommentsReturn403() {

        stubFor(get(urlPathEqualTo("/questions/123/comments"))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("rate limit exceeded")));

        ExternalApiException ex = assertThrows(
                ExternalApiClientException.class, () -> stackOverflowClient.getCommentUpdates(123L, "stackoverflow"));

        assertEquals(403, ex.getStatusCode());
        assertEquals("rate limit exceeded", ex.getBody());
    }

    @Test
    void shouldThrowExternalApiException_whenAnswersReturn403() {

        stubFor(get(urlPathEqualTo("/questions/123/answers"))
                .willReturn(aResponse().withStatus(403).withBody("blocked")));

        ExternalApiException ex = assertThrows(
                ExternalApiClientException.class, () -> stackOverflowClient.getAnswerUpdates(123L, "stackoverflow"));

        assertEquals(403, ex.getStatusCode());
        assertEquals("blocked", ex.getBody());
    }

    @Test
    void shouldSendCorrectQueryParamsForQuestionUpdate() {

        stubFor(get(urlPathEqualTo("/questions/123")).willReturn(okJson("""
                { "items": [] }
                """)));

        stackOverflowClient.getQuestionUpdateTime(123L, "stackoverflow", "test-key", "test-token");

        verify(getRequestedFor(urlPathEqualTo("/questions/123"))
                .withQueryParam("site", equalTo("stackoverflow"))
                .withQueryParam("key", equalTo("test-key"))
                .withQueryParam("access_token", equalTo("test-token")));
    }
}
