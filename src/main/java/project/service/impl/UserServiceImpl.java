package project.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import project.dto.user.request.UserUpdatePasswordRequestDto;
import project.dto.user.request.UserUpdatePersonalInfoRequestDto;
import project.dto.user.response.UserResponseDto;
import project.dto.user.response.UserUpdateResponseDto;
import project.exception.DataEditingException;
import project.mapper.UserMapper;
import project.model.User;
import project.repository.UserRepository;
import project.secure.JwtUtil;
import project.service.UserService;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto getByUsername(String username) {
        return userMapper.toDto(userRepository.getUserByUsername(username));
    }

    @Override
    @Transactional
    public void updateRole(Long id, User.Role role, String username) throws DataEditingException {
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("User with id " + id + " not found!"));
        checkUpdatingRole(user, role, username);
        userRepository.save(user.setRole(role));
        log.info("User ({}) has assigned the user (ID {}) {} role ",
                username, user.getId(), role);
    }

    @Override
    @Transactional
    public UserUpdateResponseDto updatePersonalInfo(UserUpdatePersonalInfoRequestDto requestDto,
                                            String username) throws DataEditingException {
        User user = userRepository.getUserByUsername(username);
        if (userRepository.findByUsername(requestDto.getUsername()).isPresent()
                || userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new DataEditingException("User with this email or username exists");
        }
        User updatedUser = userMapper.toModel(requestDto)
                .setRole(user.getRole())
                .setId(user.getId())
                .setDeleted(user.isDeleted())
                .setPassword(user.getPassword());
        String token = null;
        if (!updatedUser.getUsername().equals(user.getUsername())) {
            token = updateAuthentication(updatedUser);
        }
        log.info("User ({}) has updated his personal data {} ", username, updatedUser);
        return userMapper.toUpdateDto(userRepository.save(updatedUser)).setToken(token);
    }

    @Override
    @Transactional
    public void updatePassword(UserUpdatePasswordRequestDto requestDto, String username)
            throws DataEditingException {
        User user = userRepository.getUserByUsername(username);
        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPassword())) {
            throw new DataEditingException("The current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        userRepository.save(user);
        log.info("User ({}) has updated his password", username);
    }

    private String updateAuthentication(User user) {
        Authentication newAuthentication =
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
        SecurityContextHolder.getContext().setAuthentication(newAuthentication);
        return jwtUtil.generateToken(user.getUsername());
    }

    private void checkUpdatingRole(User user, User.Role role, String username)
            throws DataEditingException {
        User.Role seniorUser = userRepository.getUserByUsername(username).getRole();

        if (user.getUsername().equals(username)) {
            log.error("User ({}) tried to update his role", username);
            throw new DataEditingException("You can`t update your role");
        } else if (seniorUser.getLevel() <= user.getRole().getLevel()) {
            log.error("User ({}) tried to change user role ({}), but he does not have "
                    + "the authority to change this user's role", username, user.getUsername());
            throw new DataEditingException("You do not have the authority to change "
                    + "this user's role");
        } else if (seniorUser.getLevel() < role.getLevel()) {
            log.error("User ({}) tried to change user role ({}), but he does not "
                    + "have the authority to assign this role", username, user.getUsername());
            throw new DataEditingException("You do not have the authority to assign this role");
        } else if (user.getRole().equals(role)) {
            log.error("User ({}) tried to change user role ({}), "
                    + "but he already has this role", username, user.getUsername());
            throw new DataEditingException("This user already has this role");
        }
    }
}
