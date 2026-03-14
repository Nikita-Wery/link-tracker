package backend.academy.linktracker.bot.utils.validator;

import java.util.List;
import org.springframework.stereotype.Component;

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
