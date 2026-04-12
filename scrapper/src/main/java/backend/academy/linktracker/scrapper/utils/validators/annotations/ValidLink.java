package backend.academy.linktracker.scrapper.utils.validators.annotations;

import backend.academy.linktracker.scrapper.utils.validators.LinkValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LinkValidator.class)
@Documented
public @interface ValidLink {

    String message() default "Unsupported link";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
