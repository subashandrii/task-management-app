package project.mapper;

import org.mapstruct.Mapper;
import project.config.MapperConfig;
import project.dto.user.UserRegistrationRequestDto;
import project.dto.user.UserRegistrationResponseDto;
import project.model.User;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    User toModel(UserRegistrationRequestDto requestDto);
    
    UserRegistrationResponseDto toDto(User user);
}
