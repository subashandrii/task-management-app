package project.dto.user.response;

import lombok.Data;

@Data
public class UserTokenResponseDto {
    private String token;
    
    public UserTokenResponseDto() {
    }
    
    public UserTokenResponseDto(String token) {
        this.token = token;
    }
}
