package backend.academy.linktracker.bot.utils.validator;

import java.util.regex.Pattern;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class TagsValidator {

    private static final Pattern TAG_PATTERN = Pattern.compile("[а-яА-ЯёЁa-zA-Z0-9]+");

    public boolean validate(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String[] parts = text.split(",", -1);

        for (String part : parts) {
            String tag = part.trim();

            if (tag.isEmpty() || !TAG_PATTERN.matcher(tag).matches()) {
                return false;
            }
        }

        return true;
    }
}
