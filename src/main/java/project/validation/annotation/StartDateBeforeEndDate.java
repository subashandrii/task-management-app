package project.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import project.validation.validator.StartDateBeforeEndDateValidator;

@Constraint(validatedBy = StartDateBeforeEndDateValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface StartDateBeforeEndDate {
    String message() default "{startDate} must be before {endDate}";
    String startDateField();
    String endDateField();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
