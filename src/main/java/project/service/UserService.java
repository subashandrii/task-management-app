package project.service;

import project.dto.user.request.UserUpdatePasswordRequestDto;
import project.dto.user.request.UserUpdatePersonalInfoRequestDto;
import project.dto.user.response.UserResponseDto;
import project.dto.user.response.UserUpdateResponseDto;
import project.exception.DataEditingException;
import project.model.User;

public interface UserService {
    UserResponseDto getByUsername(String username);

    void updateRole(Long id, User.Role role, String username) throws DataEditingException;

    UserUpdateResponseDto updatePersonalInfo(UserUpdatePersonalInfoRequestDto requestDto,
                                             String username) throws DataEditingException;

    void updatePassword(UserUpdatePasswordRequestDto requestDto, String username)
            throws DataEditingException;
}
