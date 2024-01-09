package project.mapper;

import org.mapstruct.Mapper;
import project.config.MapperConfig;
import project.dto.user.request.UserRegistrationRequestDto;
import project.dto.user.request.UserUpdatePersonalInfoRequestDto;
import project.dto.user.response.UserResponseDto;
import project.dto.user.response.UserUpdateResponseDto;
import project.model.User;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    User toModel(UserRegistrationRequestDto requestDto);

    User toModel(UserUpdatePersonalInfoRequestDto userUpdatePersonalInfoRequestDto);
    
    UserResponseDto toDto(User user);

    UserUpdateResponseDto toUpdateDto(User user);
}
