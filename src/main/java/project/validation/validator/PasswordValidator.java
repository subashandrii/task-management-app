package project.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;
import project.util.PatternUtil;
import project.validation.annotation.Password;

public class PasswordValidator implements ConstraintValidator<Password, String> {
    private boolean nullable;
    
    @Override
    public void initialize(Password constraintAnnotation) {
        this.nullable = constraintAnnotation.nullable();
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return nullable;
        }
        return Pattern.compile(PatternUtil.PASSWORD_PATTERN).matcher(value).matches();
    }
}
