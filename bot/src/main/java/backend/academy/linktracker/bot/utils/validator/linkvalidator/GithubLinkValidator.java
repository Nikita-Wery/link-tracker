package backend.academy.linktracker.bot.utils.validator.linkvalidator;

import backend.academy.linktracker.bot.utils.validator.LinkValidator;
import org.springframework.stereotype.Component;
import java.net.URI;

@Component
public class GithubLinkValidator implements LinkValidator {

    @Override
    public boolean validate(String url) {
        try {
            URI uri = URI.create(url);

            if (!"github.com".equalsIgnoreCase(uri.getHost())) {
                return false;
            }

            String[] segments = uri.getPath().split("/");

            return segments.length >= 3 && !segments[1].isBlank() && !segments[2].isBlank();

        } catch (Exception e) {
            return false;
        }
    }
}
