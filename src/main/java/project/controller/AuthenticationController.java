package project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import project.dto.user.request.UserLoginRequestDto;
import project.dto.user.request.UserRegistrationRequestDto;
import project.dto.user.response.UserResponseDto;
import project.dto.user.response.UserTokenResponseDto;
import project.exception.AuthenticationException;
import project.secure.AuthenticationService;

@Tag(name = "Authentication")
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    
    @PostMapping("/register/admin")
    @Operation(summary = "Registration for administrators")
    @ResponseStatus(HttpStatus.CREATED)
    UserResponseDto registerForAdmin(@RequestBody @Valid
                                                 UserRegistrationRequestDto requestDto)
            throws AuthenticationException {
        return authenticationService.register(requestDto, true);
    }
    
    @PostMapping("/register")
    @Operation(summary = "Registration for users")
    @ResponseStatus(HttpStatus.CREATED)
    UserResponseDto register(@RequestBody @Valid UserRegistrationRequestDto requestDto)
            throws AuthenticationException {
        return authenticationService.register(requestDto, false);
    }
    
    @PostMapping("/login")
    @Operation(summary = "Login in profile")
    @ResponseStatus(HttpStatus.OK)
    UserTokenResponseDto login(@RequestBody @Valid UserLoginRequestDto requestDto)
            throws AuthenticationException {
        return authenticationService.login(requestDto);
    }
}
