package backend.academy.linktracker.scrapper.parser.impl.stackoverflow;

import backend.academy.linktracker.scrapper.parser.LinkParser;
import java.net.URI;
import org.springframework.stereotype.Component;

@Component
public class StackOverflowCommentsParser implements LinkParser<Long> {

    @Override
    public boolean supports(String url) {
        try {
            URI uri = URI.create(url);

            if (!"stackoverflow.com".equalsIgnoreCase(uri.getHost())) {
                return false;
            }

            String path = uri.getPath();

            if (path == null || path.isBlank()) {
                return false;
            }

            String[] segments = path.substring(1).split("/");

            return segments.length == 3
                    && "questions".equals(segments[0])
                    && segments[1].matches("\\d+")
                    && "comments".equals(segments[2]);

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
