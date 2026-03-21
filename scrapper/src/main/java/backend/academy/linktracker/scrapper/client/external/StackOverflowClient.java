package backend.academy.linktracker.scrapper.client.external;

import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowQuestionUpdateTime;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface StackOverflowClient {

    @GetExchange("/questions/{id}")
    StackOverflowQuestionUpdateTime getQuestionUpdateTime(
            @PathVariable long id,
            @RequestParam("site") String site,
            @RequestParam("key") String key,
            @RequestParam("access_token") String accessToken);
}
