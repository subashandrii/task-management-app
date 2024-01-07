package project.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserLoginRequestDto {
    @NotNull
    private String emailOrUsername;
    @NotNull
    private String password;
}
