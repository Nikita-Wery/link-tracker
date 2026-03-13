package backend.academy.linktracker.bot.utils.validator;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class TagsValidator {

    // TODO
    public boolean validate(String text) {
        if (text == null) return false;
//
//        String trimmed = text.trim();
//        if (trimmed.isEmpty()) return false;
//
//        String[] parts = trimmed.split(",", -1);
//        for (String raw : parts) {
//            String tag = raw.trim();
//            if (tag.isEmpty()) return false;
//            if (!ONE_TAG.matcher(tag).matches()) return false;
//        }

        return true;
    }
}
