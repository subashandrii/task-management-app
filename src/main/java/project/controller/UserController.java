package project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import project.dto.user.request.RoleRequestDto;
import project.dto.user.request.UserUpdatePasswordRequestDto;
import project.dto.user.request.UserUpdatePersonalInfoRequestDto;
import project.dto.user.response.UserResponseDto;
import project.dto.user.response.UserUpdateResponseDto;
import project.exception.DataEditingException;
import project.model.User;
import project.service.UserService;

@Tag(name = "User management")
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get user`s profile info")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto getProfileInfo(Authentication authentication) {
        return userService.getByUsername(authentication.getName());
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Update user role")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public void updateUserRole(@PathVariable Long id,
                                           @RequestBody @Valid RoleRequestDto roleRequestDto,
                                           Authentication authentication)
                                            throws DataEditingException {
        userService.updateRole(id, User.Role.valueOf(roleRequestDto.getRole()),
                authentication.getName());
    }

    @PutMapping("/me")
    @Operation(summary = "Update user`s personal info")
    @ResponseStatus(HttpStatus.OK)
    public UserUpdateResponseDto updatePersonalInfo(
            @RequestBody @Valid UserUpdatePersonalInfoRequestDto userUpdatePersonalInfoRequestDto,
            Authentication authentication) throws DataEditingException {
        return userService.updatePersonalInfo(userUpdatePersonalInfoRequestDto,
                authentication.getName());
    }

    @PutMapping("/me/password")
    @Operation(summary = "Update user`s password")
    @ResponseStatus(HttpStatus.OK)
    public void updatePassword(@RequestBody @Valid UserUpdatePasswordRequestDto
                                                      userUpdatePasswordRequestDto,
                                          Authentication authentication)
                                                        throws DataEditingException {
        userService.updatePassword(userUpdatePasswordRequestDto, authentication.getName());
    }
}
