package backend.academy.linktracker.bot.utils.validator.linkvalidator;

import backend.academy.linktracker.bot.utils.validator.LinkValidator;
import java.net.URI;
import org.springframework.stereotype.Component;

@Component
public class StackOverflowLinkValidator implements LinkValidator {

    @Override
    public boolean validate(String url) {
        try {
            URI uri = URI.create(url);

            if (!"stackoverflow.com".equalsIgnoreCase(uri.getHost())) {
                return false;
            }

            String[] segments = uri.getPath().split("/");

            return segments.length >= 3 && "questions".equals(segments[1]) && segments[2].matches("\\d+");

        } catch (Exception e) {
            return false;
        }
    }
}
