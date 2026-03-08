package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowQuestionUpdateTime;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface StackOverflowClient {

    //TODO: добавить токен и подумать как это лучше сделать
    @GetExchange("/2.3/questions/{id}")
    StackOverflowQuestionUpdateTime getQuestionUpdateTime(
            @PathVariable long id
    );

}
