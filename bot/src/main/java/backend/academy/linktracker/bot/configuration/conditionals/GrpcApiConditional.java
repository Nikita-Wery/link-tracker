package backend.academy.linktracker.bot.configuration.conditionals;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class GrpcApiConditional implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String mode = context.getEnvironment().getProperty("app.client.scrapper.api");
        return "both".equals(mode) || "grpc".equals(mode);
    }
}
