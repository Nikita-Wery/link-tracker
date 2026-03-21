package backend.academy.linktracker.bot.configuration.conditionals;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class RestApiConditional implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String mode = context.getEnvironment().getProperty("app.client.scrapper.api");

        boolean matchIfMissing = true;

        if (mode == null) {
            return matchIfMissing;
        }

        return "both".equals(mode) || "rest".equals(mode);
    }
}
