package backend.academy.linktracker.bot.utils.validator;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class LinkValidationProcessor {

    private final List<LinkValidator> linkValidators;

    public LinkValidationProcessor(List<LinkValidator> linkValidators) {
        this.linkValidators = linkValidators;
    }

    public boolean isValid(String url) {
        return linkValidators.stream().anyMatch(linkValidator -> linkValidator.validate(url));
    }
}
