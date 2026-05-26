package backend.academy.linktracker.scrapper.client.external;

import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowAnswerResponse;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowCommentResponse;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowQuestionUpdateTime;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowWrapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
@CircuitBreaker(name = "stackOverflowApi")
public interface StackOverflowClient {

    @GetExchange("/questions/{id}")
    StackOverflowQuestionUpdateTime getQuestionUpdateTime(
            @PathVariable long id,
            @RequestParam("site") String site,
            @RequestParam("key") String key,
            @RequestParam("access_token") String accessToken);

    @GetExchange("/questions/{id}/answers")
    StackOverflowWrapper<StackOverflowAnswerResponse> getAnswerUpdates(
            @PathVariable long id, @RequestParam("site") String site);

    @GetExchange("/questions/{id}/comments")
    StackOverflowWrapper<StackOverflowCommentResponse> getCommentUpdates(
            @PathVariable long id, @RequestParam("site") String site);
}
