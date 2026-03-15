package backend.academy.linktracker.bot.utils.validator;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
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
