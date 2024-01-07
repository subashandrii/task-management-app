package project.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;
import project.util.PatternUtil;
import project.validation.annotation.Email;

public class EmailValidator implements ConstraintValidator<Email, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return Pattern.compile(PatternUtil.EMAIL_PATTERN).matcher(value).matches();
    }
}
