package project.dto.user;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserRegistrationResponseDto {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
}
