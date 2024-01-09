package project.dto.user.request;

import lombok.Data;
import lombok.experimental.Accessors;
import project.model.User;
import project.validation.annotation.EnumValueCheck;

@Data
@Accessors(chain = true)
public class RoleRequestDto {
    @EnumValueCheck(User.Role.class)
    private String role;
}
