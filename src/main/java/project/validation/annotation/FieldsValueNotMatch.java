package project.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import project.validation.validator.FieldsValueNotMatchValidator;

@Constraint(validatedBy = FieldsValueNotMatchValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldsValueNotMatch {
    String message() default "Fields values must not match!";
    String field();
    String fieldNotMatch();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
