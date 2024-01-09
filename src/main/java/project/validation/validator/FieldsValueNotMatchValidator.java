package project.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import org.springframework.beans.BeanWrapperImpl;
import project.validation.annotation.FieldsValueNotMatch;

public class FieldsValueNotMatchValidator
        implements ConstraintValidator<FieldsValueNotMatch, Object> {
    private String field;
    private String fieldNotMatch;

    @Override
    public void initialize(FieldsValueNotMatch constraintAnnotation) {
        this.field = constraintAnnotation.field();
        this.fieldNotMatch = constraintAnnotation.fieldNotMatch();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Object fieldValue = new BeanWrapperImpl(value).getPropertyValue(field);
        Object fieldNotMatchValue = new BeanWrapperImpl(value).getPropertyValue(fieldNotMatch);
        return fieldValue == null && fieldNotMatchValue == null
                || !Objects.equals(fieldValue, fieldNotMatchValue);
    }
}
