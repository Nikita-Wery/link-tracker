package backend.academy.linktracker.scrapper.utils.validators;

import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.utils.validators.annotations.ValidLink;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class LinkValidator implements ConstraintValidator<ValidLink, String> {

    @Override
    public boolean isValid(String url, ConstraintValidatorContext context) {

        if (url == null || url.isBlank()) {
            return true;
        }

        return Arrays.stream(ResourceType.values())
                .anyMatch(type -> type.parser().supports(url));
    }
}
