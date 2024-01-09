package project.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import project.validation.validator.EnumValueCheckValidator;

@Constraint(validatedBy = EnumValueCheckValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValueCheck {
    String message() default "must be one of these values: {values}";
    Class<? extends Enum<?>> value();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
