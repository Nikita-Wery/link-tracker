package backend.academy.linktracker.scrapper.client.responsehandler.statuscodehandler.stackoverflow;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import java.util.Set;

public class StackOverflowQuestionStatusCodeHandler extends StackOverflowStatusCodeDefaultHandler {

    public StackOverflowQuestionStatusCodeHandler() {
        // TODO: изменить на нужный код
        super(Set.of(400));
    }

    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) {
        // TODO: добавить реализацию
    }
}
