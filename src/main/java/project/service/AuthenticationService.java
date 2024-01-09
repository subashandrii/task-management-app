package project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.dto.user.request.UserLoginRequestDto;
import project.dto.user.request.UserRegistrationRequestDto;
import project.dto.user.response.UserResponseDto;
import project.dto.user.response.UserTokenResponseDto;
import project.exception.AuthenticationException;
import project.mapper.UserMapper;
import project.model.User;
import project.repository.UserRepository;
import project.secure.JwtUtil;

@RequiredArgsConstructor
@Component
@Log4j2
public class AuthenticationService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    
    @Transactional
    public UserResponseDto register(UserRegistrationRequestDto requestDto,
                                    boolean isItAdmin) throws AuthenticationException {
        if (userRepository.findByUsername(requestDto.getUsername()).isPresent()
                    || userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new AuthenticationException("Unable to complete registration!");
        }
        User user = userMapper.toModel(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        if (isItAdmin) {
            user.setRole(User.Role.ADMIN);
        }
        User savedUser = userRepository.save(user);
        log.info("Registered new user {}", user);
        return userMapper.toDto(savedUser);
    }
    
    @Transactional
    public UserTokenResponseDto login(UserLoginRequestDto requestDto)
            throws AuthenticationException {
        User user = checkCredentials(requestDto);
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(),
                        requestDto.getPassword()));
        String token = jwtUtil.generateToken(authentication.getName());
        log.info("User ({}) logged in", user.getUsername());
        return new UserTokenResponseDto(token);
    }
    
    private User checkCredentials(UserLoginRequestDto requestDto) throws AuthenticationException {
        User user = (requestDto.getEmailOrUsername().contains("@")
                             ? userRepository.findByEmail(requestDto.getEmailOrUsername())
                             : userRepository.findByUsername(requestDto.getEmailOrUsername()))
                            .orElseThrow(() -> new AuthenticationException(
                                    "Invalid username or password"));
        if (passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            return user;
        }
        log.warn("User ({}) tried to log in, but entered an incorrect password",
                user.getUsername());
        throw new AuthenticationException("Invalid username or password");
    }
}
