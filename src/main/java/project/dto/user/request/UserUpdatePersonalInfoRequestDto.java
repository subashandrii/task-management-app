package project.dto.user.request;

import lombok.Data;
import lombok.experimental.Accessors;
import project.validation.annotation.Email;
import project.validation.annotation.Name;
import project.validation.annotation.Username;

@Data
@Accessors(chain = true)
public class UserUpdatePersonalInfoRequestDto {
    @Username
    private String username;
    @Email
    private String email;
    @Name
    private String firstName;
    @Name
    private String lastName;

}
