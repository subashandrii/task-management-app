package project.dto.user.request;

import lombok.Data;
import lombok.experimental.Accessors;
import project.validation.annotation.FieldsValueMatch;
import project.validation.annotation.FieldsValueNotMatch;
import project.validation.annotation.Password;

@Data
@Accessors(chain = true)
@FieldsValueMatch(
        field = "newPassword",
        fieldMatch = "repeatNewPassword",
        message = "passwords do not match!")
@FieldsValueNotMatch(
        field = "currentPassword",
        fieldNotMatch = "newPassword",
        message = "current and new passwords must not match!")
public class UserUpdatePasswordRequestDto {
    @Password
    private String currentPassword;
    @Password
    private String newPassword;
    private String repeatNewPassword;
}
