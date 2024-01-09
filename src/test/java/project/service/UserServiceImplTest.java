package project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;
import project.dto.user.request.UserUpdatePasswordRequestDto;
import project.dto.user.request.UserUpdatePersonalInfoRequestDto;
import project.dto.user.response.UserResponseDto;
import project.dto.user.response.UserUpdateResponseDto;
import project.exception.DataEditingException;
import project.mapper.UserMapper;
import project.model.User;
import project.repository.UserRepository;
import project.secure.JwtUtil;
import project.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;
    private UserResponseDto userResponseDto;
    private UserUpdatePersonalInfoRequestDto userUpdatePersonalInfoRequestDto;
    private UserUpdateResponseDto userUpdateResponseDto;
    private UserUpdatePasswordRequestDto userUpdatePasswordRequestDto;
    private User user;
    private User adminUser;

    @BeforeEach
    public void setUp() {
        userResponseDto = new UserResponseDto()
                .setId(2L)
                .setUsername("alice123")
                .setFirstName("Alice")
                .setLastName("Lastname");

        user = new User()
                .setId(2L)
                .setUsername("alice123")
                .setEmail("alice@email.com")
                .setPassword("Password123!!!")
                .setFirstName("Alice")
                .setLastName("Lastname");

        adminUser = new User()
                .setId(3L)
                .setUsername("john123")
                .setEmail("john@email.com")
                .setPassword("Password123!!!")
                .setFirstName("John")
                .setLastName("Lastname")
                .setRole(User.Role.ADMIN);

        userUpdatePersonalInfoRequestDto = new UserUpdatePersonalInfoRequestDto()
                .setEmail("alice1@email.com")
                .setUsername("alice1234")
                .setFirstName("Aliceeee")
                .setLastName("Lastnameeee");

        userUpdateResponseDto = new UserUpdateResponseDto()
                .setId(2L)
                .setEmail("alice1@email.com")
                .setUsername("alice1234")
                .setFirstName("Aliceeee")
                .setLastName("Lastnameeee");

        userUpdatePasswordRequestDto = new UserUpdatePasswordRequestDto()
                .setCurrentPassword("Password123!!!")
                .setNewPassword("Password987???")
                .setRepeatNewPassword("Password987???");
    }

    @Test
    @DisplayName("Get user by username when user with this username exists")
    public void getUser_UserExists_ReturnsDto() {
        String username = "alice123";

        when(userRepository.getUserByUsername(username)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userResponseDto);
        UserResponseDto actual = userService.getByUsername(username);

        assertNotNull(actual);
        assertEquals(user.getId(), actual.getId());
    }

    @Test
    @DisplayName("Update role when data is correct")
    public void updateRole_DataIsCorrect_Success() throws DataEditingException {
        Long id = 2L;
        final User.Role role = User.Role.ADMIN;
        String username = "john123";

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.getUserByUsername(username)).thenReturn(adminUser);
        when(userRepository.save(user)).thenReturn(user);
        userService.updateRole(id, role, username);

        assertEquals(role, userRepository.findById(id).get().getRole());
        verify(userRepository, times(1)).save(user.setRole(role));
    }

    @Test
    @DisplayName("Update role when user with this id does not exist")
    public void updateRole_UserWithThisIdDoesNotExist_ReturnsException() {
        Long id = 100L;
        User.Role role = User.Role.ADMIN;
        String username = "john123";

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> userService.updateRole(id, role, username));
        assertEquals("User with id " + id + " not found!", exception.getMessage());
    }

    @Test
    @DisplayName("Update role when user wants change his role")
    public void updateRole_UserChangesHisRole_ReturnsException() {
        Long id = 3L;
        User.Role role = User.Role.USER;
        String username = "john123";

        when(userRepository.findById(id)).thenReturn(Optional.of(adminUser));
        when(userRepository.getUserByUsername(username)).thenReturn(adminUser);

        Exception exception = assertThrows(DataEditingException.class,
                () -> userService.updateRole(id, role, username));
        assertEquals("You can`t update your role", exception.getMessage());
    }

    @Test
    @DisplayName("Update role of the senior user")
    public void updateRole_UpdateRoleOfSeniorUser_ReturnsException() {
        Long id = 2L;
        User.Role role = User.Role.ADMIN;
        String username = "john123";

        when(userRepository.findById(id)).thenReturn(Optional.of(user.setRole(User.Role.ADMIN)));
        when(userRepository.getUserByUsername(username)).thenReturn(adminUser);

        Exception exception = assertThrows(DataEditingException.class,
                () -> userService.updateRole(id, role, username));
        assertEquals("You do not have the authority to change "
                + "this user's role", exception.getMessage());
    }

    @Test
    @DisplayName("Update role when user already has this role")
    public void updateRole_UserAlreadyHasThisRole_ReturnsException() {
        Long id = 2L;
        User.Role role = User.Role.USER;
        String username = "john123";

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.getUserByUsername(username)).thenReturn(adminUser);

        Exception exception = assertThrows(DataEditingException.class,
                () -> userService.updateRole(id, role, username));
        assertEquals("This user already has this role", exception.getMessage());
    }

    @Test
    @DisplayName("Update personal info with correct dto")
    public void updatePersonalInfo_DtoIsCorrect_ReturnsResponseDto() throws DataEditingException {
        String username = "alice123";
        String token = "token";
        User updatedUser = new User().setEmail("alice1@email.com")
                .setUsername("alice1234")
                .setFirstName("Aliceeee")
                .setLastName("Lastnameeee");

        when(userRepository.getUserByUsername(username)).thenReturn(user);
        when(userRepository.findByUsername(userUpdatePersonalInfoRequestDto.getUsername()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail(userUpdatePersonalInfoRequestDto.getEmail()))
                .thenReturn(Optional.empty());
        when(userMapper.toModel(userUpdatePersonalInfoRequestDto)).thenReturn(updatedUser);
        when(jwtUtil.generateToken(updatedUser.getUsername())).thenReturn(token);
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);
        when(userMapper.toUpdateDto(updatedUser)).thenReturn(userUpdateResponseDto);
        UserUpdateResponseDto actual =
                userService.updatePersonalInfo(userUpdatePersonalInfoRequestDto, username);

        assertNotNull(actual);
        assertNotNull(actual.getToken());
        EqualsBuilder.reflectionEquals(userUpdateResponseDto, actual, "token");

    }

    @Test
    @DisplayName("Update personal info with correct dto without new username")
    public void updatePersonalInfo_DtoWithoutNewUsername_ReturnsResponseDtoWithoutToken()
            throws DataEditingException {
        String username = "alice123";
        userUpdatePersonalInfoRequestDto.setUsername(username);
        User updatedUser = new User().setEmail("alice1@email.com")
                .setUsername(username)
                .setFirstName("Aliceeee")
                .setLastName("Lastnameeee");

        when(userRepository.getUserByUsername(username)).thenReturn(user);
        when(userRepository.findByUsername(userUpdatePersonalInfoRequestDto.getUsername()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail(userUpdatePersonalInfoRequestDto.getEmail()))
                .thenReturn(Optional.empty());
        when(userMapper.toModel(userUpdatePersonalInfoRequestDto)).thenReturn(updatedUser);
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);
        when(userMapper.toUpdateDto(updatedUser)).thenReturn(userUpdateResponseDto);
        UserUpdateResponseDto actual =
                userService.updatePersonalInfo(userUpdatePersonalInfoRequestDto, username);

        assertNotNull(actual);
        assertNull(actual.getToken());
        verify(jwtUtil, times(0)).generateToken(username);
        EqualsBuilder.reflectionEquals(userUpdateResponseDto, actual, "token");
    }

    @Test
    @DisplayName("Update personal info when user with new username already exists")
    public void updatePersonalInfo_UserWithNewUsernameExists_ReturnsException() {
        String username = "alice123";
        User existingUser = new User().setUsername("alice1234");

        when(userRepository.getUserByUsername(username)).thenReturn(user);
        when(userRepository.findByUsername(userUpdatePersonalInfoRequestDto.getUsername()))
                .thenReturn(Optional.of(existingUser));

        Exception exception = assertThrows(DataEditingException.class,
                () -> userService.updatePersonalInfo(userUpdatePersonalInfoRequestDto, username));
        assertEquals("User with this email or username exists", exception.getMessage());
    }

    @Test
    @DisplayName("Update personal info when user with new email already exists")
    public void updatePersonalInfo_UserWithNewEmailExists_ReturnsException() {
        String username = "alice123";
        User existingUser = new User().setEmail("alice1234@email.com");

        when(userRepository.getUserByUsername(username)).thenReturn(user);
        when(userRepository.findByEmail(userUpdatePersonalInfoRequestDto.getEmail()))
                .thenReturn(Optional.of(existingUser));

        Exception exception = assertThrows(DataEditingException.class,
                () -> userService.updatePersonalInfo(userUpdatePersonalInfoRequestDto, username));
        assertEquals("User with this email or username exists", exception.getMessage());
    }

    @Test
    @DisplayName("Update password with correct dto")
    public void updatePassword_DtoIsCorrect_Success() throws DataEditingException {
        String username = "alice123";
        String encodedPassword = "encoded_password";

        when(userRepository.getUserByUsername(username)).thenReturn(user);
        when(passwordEncoder.matches(userUpdatePasswordRequestDto.getCurrentPassword(),
                user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(userUpdatePasswordRequestDto.getNewPassword()))
                .thenReturn(encodedPassword);
        when(userRepository.save(user)).thenReturn(user);
        userService.updatePassword(userUpdatePasswordRequestDto, username);

        assertEquals(encodedPassword, user.getPassword());
    }

    @Test
    @DisplayName("Update password when current passwords from db and dto do not match")
    public void updatePassword_Current_Success() {
        String username = "alice123";
        userUpdatePasswordRequestDto.setCurrentPassword("Password234!!!");

        when(userRepository.getUserByUsername(username)).thenReturn(user);
        when(passwordEncoder.matches(userUpdatePasswordRequestDto.getCurrentPassword(),
                user.getPassword())).thenReturn(false);

        Exception exception = assertThrows(DataEditingException.class,
                () -> userService.updatePassword(userUpdatePasswordRequestDto, username));
        assertEquals("The current password is incorrect", exception.getMessage());
    }
}
