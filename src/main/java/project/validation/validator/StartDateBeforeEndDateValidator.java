package project.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import org.springframework.beans.BeanWrapperImpl;
import project.validation.annotation.StartDateBeforeEndDate;

public class StartDateBeforeEndDateValidator
        implements ConstraintValidator<StartDateBeforeEndDate, Object> {
    private String startDate;
    private String endDate;

    @Override
    public void initialize(StartDateBeforeEndDate constraintAnnotation) {
        startDate = constraintAnnotation.startDateField();
        endDate = constraintAnnotation.endDateField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                startDate + " must be before " + endDate
        ).addConstraintViolation();

        LocalDate startDateValue = (LocalDate) new BeanWrapperImpl(value)
                .getPropertyValue(startDate);
        LocalDate endDateValue = (LocalDate) new BeanWrapperImpl(value)
                .getPropertyValue(endDate);

        if (startDateValue == null && endDateValue == null) {
            return true;
        } else if (startDateValue == null || endDateValue == null) {
            return false;
        }
        return startDateValue.isBefore(endDateValue) || startDateValue.equals(endDateValue);
    }
}
