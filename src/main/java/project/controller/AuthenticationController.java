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
import project.dto.user.UserLoginRequestDto;
import project.dto.user.UserLoginResponseDto;
import project.dto.user.UserRegistrationRequestDto;
import project.dto.user.UserRegistrationResponseDto;
import project.exception.AuthenticationException;
import project.service.AuthenticationService;

@Tag(name = "Authentication")
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    
    @PostMapping("/register/admin")
    @Operation(summary = "Registration for administrators")
    @ResponseStatus(HttpStatus.CREATED)
    UserRegistrationResponseDto registerForAdmin(@RequestBody @Valid
                                                 UserRegistrationRequestDto requestDto)
            throws AuthenticationException {
        return authenticationService.register(requestDto, true);
    }
    
    @PostMapping("/register")
    @Operation(summary = "Registration for users")
    @ResponseStatus(HttpStatus.CREATED)
    UserRegistrationResponseDto register(@RequestBody @Valid UserRegistrationRequestDto requestDto)
            throws AuthenticationException {
        return authenticationService.register(requestDto, false);
    }
    
    @PostMapping("/login")
    @Operation(summary = "Login in profile")
    @ResponseStatus(HttpStatus.OK)
    UserLoginResponseDto login(@RequestBody @Valid UserLoginRequestDto requestDto)
            throws AuthenticationException {
        return authenticationService.login(requestDto);
    }
}
