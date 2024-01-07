package project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;
import project.dto.user.UserLoginRequestDto;
import project.dto.user.UserLoginResponseDto;
import project.dto.user.UserRegistrationRequestDto;
import project.dto.user.UserRegistrationResponseDto;
import project.exception.AuthenticationException;
import project.mapper.UserMapper;
import project.model.User;
import project.repository.UserRepository;
import project.secure.JwtUtil;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private Authentication authentication;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private AuthenticationService authenticationService;
    private UserRegistrationRequestDto userRegistrationRequestDto;
    private UserRegistrationResponseDto userRegistrationResponseDto;
    private UserLoginRequestDto userLoginRequestDto;
    private User user;
    
    @BeforeEach
    public void setUp() {
        userRegistrationRequestDto = new UserRegistrationRequestDto()
                                             .setUsername("bob123")
                                             .setEmail("bob@email.com")
                                             .setFirstName("Bob")
                                             .setLastName("Lastname")
                                             .setPassword("Password123!!!")
                                             .setRepeatPassword("Password123!!!");
        
        userRegistrationResponseDto = new UserRegistrationResponseDto()
                                              .setId(1L)
                                              .setUsername("bob123")
                                              .setFirstName("Bob")
                                              .setLastName("Lastname");
        
        userLoginRequestDto = new UserLoginRequestDto()
                                      .setPassword("Password123!!!");
        
        user = new User()
                       .setId(1L)
                       .setUsername("bob123")
                       .setEmail("bob@email.com")
                       .setPassword("Password123!!!")
                       .setFirstName("Bob")
                       .setLastName("Lastname");
    }
    
    @Test
    @DisplayName("Registration of a new user with admin role by correct dto")
    public void register_RequestDtoIsCorrectAndRoleIsAdmin_ReturnsResponseDto()
            throws AuthenticationException {
        User userWithoutId = user.setId(null);
        
        when(userRepository.findByUsername(userRegistrationRequestDto.getUsername()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail(userRegistrationRequestDto.getEmail()))
                .thenReturn(Optional.empty());
        when(userMapper.toModel(userRegistrationRequestDto)).thenReturn(userWithoutId);
        when(passwordEncoder.encode(userWithoutId.getPassword())).thenReturn(any());
        when(userRepository.save(userWithoutId)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userRegistrationResponseDto);
        UserRegistrationResponseDto actual =
                authenticationService.register(userRegistrationRequestDto, true);
        
        Assertions.assertNotNull(actual);
        EqualsBuilder.reflectionEquals(userRegistrationResponseDto, actual);
    }
    
    @Test
    @DisplayName("Registration of a new user when there is already a user with this email")
    public void register_UserWithThisEmailIsAlreadyExist_ReturnsException() {
        when(userRepository.findByEmail(userRegistrationRequestDto.getEmail()))
                .thenReturn(Optional.of(user));
        
        Exception exception = assertThrows(AuthenticationException.class,
                () -> authenticationService.register(userRegistrationRequestDto, false));
        assertEquals("Unable to complete registration!", exception.getMessage());
    }
    
    @Test
    @DisplayName("Registration of a new user when there is already a user with this username")
    public void register_UserWithThisUsernameIsAlreadyExist_ReturnsException() {
        when(userRepository.findByUsername(userRegistrationRequestDto.getUsername()))
                .thenReturn(Optional.of(user));
        
        Exception exception = assertThrows(AuthenticationException.class,
                () -> authenticationService.register(userRegistrationRequestDto, false));
        assertEquals("Unable to complete registration!", exception.getMessage());
    }
    
    @Test
    @DisplayName("User log in with correct username and password")
    public void login_UsernameIsCorrect_ReturnsResponseDto() throws AuthenticationException {
        userLoginRequestDto.setEmailOrUsername("bob123");
        String token = "token";
        
        when(userRepository.findByUsername(userLoginRequestDto.getEmailOrUsername()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(userLoginRequestDto.getPassword(), user.getPassword()))
                .thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(authentication.getName())).thenReturn(token);
        UserLoginResponseDto actual = authenticationService.login(userLoginRequestDto);
        
        assertNotNull(actual);
        assertEquals(token, actual.getToken());
    }
    
    @Test
    @DisplayName("User log in with correct email and password")
    public void login_EmailIsCorrect_ReturnsResponseDto() throws AuthenticationException {
        userLoginRequestDto.setEmailOrUsername("bob@email.com");
        String token = "token";
        
        when(userRepository.findByEmail(userLoginRequestDto.getEmailOrUsername()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(userLoginRequestDto.getPassword(), user.getPassword()))
                .thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(authentication.getName())).thenReturn(token);
        UserLoginResponseDto actual = authenticationService.login(userLoginRequestDto);
        
        assertNotNull(actual);
        assertEquals(token, actual.getToken());
    }
    
    @Test
    @DisplayName("User tried to log in with not correct username")
    public void login_UsernameIsNotCorrect_ReturnsException() {
        userLoginRequestDto.setEmailOrUsername("bobob321");
        
        when(userRepository.findByUsername(userLoginRequestDto.getEmailOrUsername()))
                .thenReturn(Optional.empty());
        Exception exception = assertThrows(AuthenticationException.class,
                () -> authenticationService.login(userLoginRequestDto));
        assertEquals("Invalid username or password", exception.getMessage());
    }
    
    @Test
    @DisplayName("User tried to log in with not correct email")
    public void login_EmailIsNotCorrect_ReturnsException() {
        userLoginRequestDto.setEmailOrUsername("bobob321@email.com");
        
        when(userRepository.findByEmail(userLoginRequestDto.getEmailOrUsername()))
                .thenReturn(Optional.empty());
        Exception exception = assertThrows(AuthenticationException.class,
                () -> authenticationService.login(userLoginRequestDto));
        assertEquals("Invalid username or password", exception.getMessage());
    }
    
    @Test
    @DisplayName("User tried to log in with not correct password")
    public void login_PasswordIsNotCorrect_ReturnsException() {
        userLoginRequestDto.setEmailOrUsername("bob@email.com");
        userLoginRequestDto.setPassword("Password321???");
        
        when(userRepository.findByEmail(userLoginRequestDto.getEmailOrUsername()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(userLoginRequestDto.getPassword(), user.getPassword()))
                .thenReturn(false);
        
        Exception exception = assertThrows(AuthenticationException.class,
                () -> authenticationService.login(userLoginRequestDto));
        assertEquals("Invalid username or password", exception.getMessage());
    }
}
