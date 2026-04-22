package backend.academy.linktracker.scrapper.parser.impl.stackoverflow;

import backend.academy.linktracker.scrapper.parser.LinkParser;
import java.net.URI;
import org.springframework.stereotype.Component;

@Component
public class StackOverflowAnswersParser implements LinkParser<Long> {

    @Override
    public boolean supports(String url) {
        try {
            URI uri = URI.create(url);

            if (!"stackoverflow.com".equalsIgnoreCase(uri.getHost())) {
                return false;
            }

            String[] segments = uri.getPath().split("/");

            return segments.length == 3
                    && "questions".equals(segments[1])
                    && segments[2].matches("\\d+")
                    && "answers".equals(segments[3]);

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Long parse(URI url) {

        String[] segments = url.getPath().split("/");

        long questionId = Long.parseLong(segments[2]);

        return questionId;
    }
}
