package project.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import project.validation.annotation.EnumValueCheck;

public class EnumValueCheckValidator implements ConstraintValidator<EnumValueCheck, String> {
    private Class<? extends Enum> enumClass;

    @Override
    public void initialize(EnumValueCheck constraintAnnotation) {
        enumClass = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        Enum<?>[] enumConstants = enumClass.getEnumConstants();

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                "must be one of these values: " + Arrays.toString(enumConstants)
        ).addConstraintViolation();

        return Arrays.stream(enumConstants)
                .map(Enum::toString)
                .anyMatch(enumValue -> enumValue.equals(value));

    }
}
