package project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;
import project.dto.user.request.RoleRequestDto;
import project.dto.user.request.UserUpdatePasswordRequestDto;
import project.dto.user.request.UserUpdatePersonalInfoRequestDto;
import project.dto.user.response.UserResponseDto;
import project.dto.user.response.UserUpdateResponseDto;
import project.model.User;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIntegrationTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private UserUpdatePersonalInfoRequestDto userUpdatePersonalInfoRequestDto;
    private UserUpdatePasswordRequestDto userUpdatePasswordRequestDto;
    private UserUpdateResponseDto userUpdateResponseDto;
    private UserResponseDto userResponseDto;

    @BeforeAll
    static void beforeAll(@Autowired DataSource dataSource,
                          @Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        teardown(dataSource);
    }

    @BeforeEach
    public void setUp() {
        userUpdatePersonalInfoRequestDto = new UserUpdatePersonalInfoRequestDto()
                .setUsername("john1234")
                .setEmail("john1@email.com")
                .setFirstName("Johnnnn")
                .setLastName("Lastnameeee");

        userUpdatePasswordRequestDto = new UserUpdatePasswordRequestDto()
                .setCurrentPassword("Password123!!!")
                .setNewPassword("Password234???")
                .setRepeatNewPassword("Password234???");

        userUpdateResponseDto = new UserUpdateResponseDto()
                .setId(3L)
                .setUsername("john1234")
                .setEmail("john1@email.com")
                .setFirstName("Johnnnn")
                .setLastName("Lastnameeee");

        userResponseDto = new UserResponseDto()
                .setId(3L)
                .setUsername("john123")
                .setFirstName("John")
                .setLastName("Lastname")
                .setRole("ADMIN")
                .setEmail("john@email.com");
    }

    @SneakyThrows
    private static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/remove-all-tables.sql"));
        }
    }

    @AfterEach
    void afterEach(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @Test
    @DisplayName("Get personal info about profile")
    @WithMockUser(username = "john123", roles = {"ADMIN"})
    @Sql(scripts = "classpath:database/add-users-to-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void getProfileInfo_ReturnsResponseDto() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/users/me"))
                                .andExpect(status().isOk())
                                .andReturn();
        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserResponseDto.class);

        assertNotNull(actual);
        EqualsBuilder.reflectionEquals(userResponseDto, actual);
    }

    @Test
    @DisplayName("Update user role by valid dto")
    @WithMockUser(username = "john123", roles = {"ADMIN"})
    @Sql(scripts = "classpath:database/add-users-to-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void updateUserRole_DtoIsValid_Success() throws Exception {
        String roleName = "ADMIN";
        String jsonRequest =
                objectMapper.writeValueAsString(new RoleRequestDto().setRole(roleName));

        mockMvc.perform(
                put("/users/2/role")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        String userRole = jdbcTemplate.queryForObject(
                "SELECT role FROM users WHERE id = 2", String.class);

        assertEquals("ADMIN", userRole);
    }

    @Test
    @DisplayName("Update user role by not valid dto")
    @WithMockUser(username = "john123", roles = {"ADMIN"})
    @Sql(scripts = "classpath:database/add-users-to-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void updateUserRole_DtoIsNotValid_ReturnsException() throws Exception {
        String roleName = "ADMINNN";
        String jsonRequest =
                objectMapper.writeValueAsString(new RoleRequestDto().setRole(roleName));

        MvcResult result = mockMvc.perform(
                        put("/users/2/role")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        String errorMessage = objectMapper.convertValue(
                getErrorResponse(result), String[].class)[0];

        assertEquals("role must be one of these values: "
                + Arrays.toString(User.Role.values()), errorMessage);
    }

    @Test
    @DisplayName("User tries to update user role")
    @WithMockUser(username = "bob123")
    @Sql(scripts = "classpath:database/add-users-to-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void updateUserRole_UserTriesToUpdateRole_ReturnsException() throws Exception {
        String roleName = "ADMIN";
        String jsonRequest =
                objectMapper.writeValueAsString(new RoleRequestDto().setRole(roleName));

        mockMvc.perform(put("/users/2/role")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Update user personal info by valid dto")
    @WithMockUser(username = "john123", roles = {"ADMIN"})
    @Sql(scripts = "classpath:database/add-users-to-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void updatePersonalInfo_DtoIsValid_ReturnsResponseDto() throws Exception {
        String jsonRequest =
                objectMapper.writeValueAsString(userUpdatePersonalInfoRequestDto);

        MvcResult result = mockMvc.perform(
                        put("/users/me")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        UserUpdateResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserUpdateResponseDto.class);

        assertNotNull(actual);
        assertNotNull(actual.getToken());
        EqualsBuilder.reflectionEquals(userUpdateResponseDto, actual, "id", "token");
    }

    @Test
    @DisplayName("Update user personal info by not valid dto")
    @WithMockUser(username = "john123", roles = {"ADMIN"})
    @Sql(scripts = "classpath:database/add-users-to-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void updatePersonalInfo_DtoIsNotValid_ReturnsException() throws Exception {
        UserUpdatePersonalInfoRequestDto requestDto = new UserUpdatePersonalInfoRequestDto()
                .setUsername("john")
                .setEmail("john")
                .setFirstName("J")
                .setLastName("L");
        String jsonRequest =
                objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(
                        put("/users/me")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        List<String> errorMessages = objectMapper.convertValue(
                getErrorResponse(result), List.class);

        assertEquals(4, errorMessages.size());
        assertTrue(errorMessages.contains("username format is not valid"));
        assertTrue(errorMessages.contains("email format is not valid"));
        assertTrue(errorMessages.contains("firstName format is not valid"));
        assertTrue(errorMessages.contains("lastName format is not valid"));
    }

    @Test
    @DisplayName("Update user password by valid dto")
    @WithMockUser(username = "john123", roles = {"ADMIN"})
    @Sql(scripts = "classpath:database/add-users-to-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void updatePassword_DtoIsValid_Success() throws Exception {
        jdbcTemplate.update("UPDATE users SET password = ? WHERE id = 3",
                passwordEncoder.encode(userUpdatePasswordRequestDto.getCurrentPassword()));
        String jsonRequest =
                objectMapper.writeValueAsString(userUpdatePasswordRequestDto);

        mockMvc.perform(put("/users/me/password")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        String password = jdbcTemplate.queryForObject(
                "SELECT password FROM users WHERE id = 3", String.class);

        assertFalse(passwordEncoder.matches(
                userUpdatePasswordRequestDto.getCurrentPassword(), password));
        assertTrue(passwordEncoder.matches(
                userUpdatePasswordRequestDto.getNewPassword(), password));
    }

    @Test
    @DisplayName("Update user password by not valid dto")
    @WithMockUser(username = "john123", roles = {"ADMIN"})
    @Sql(scripts = "classpath:database/add-users-to-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void updatePassword_DtoIsNotValid_Success() throws Exception {
        UserUpdatePasswordRequestDto requestDto = new UserUpdatePasswordRequestDto()
                .setCurrentPassword("password")
                .setNewPassword("password")
                .setRepeatNewPassword("newPassword");
        String jsonRequest =
                objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(put("/users/me/password")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        List<String> errorMessages = objectMapper.convertValue(
                getErrorResponse(result), List.class);

        assertEquals(4, errorMessages.size());
        assertTrue(errorMessages.contains("currentPassword format is not valid"));
        assertTrue(errorMessages.contains("newPassword format is not valid"));
        assertTrue(errorMessages.contains("passwords do not match!"));
        assertTrue(errorMessages.contains("current and new passwords must not match!"));
    }

    @Test
    @DisplayName("Unauthorized user attempts to access endpoints intended for authorized users")
    @Sql(scripts = "classpath:database/add-users-to-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void unauthorizedUserGoesToEndpointsForAuthorizedUsers_ReturnsException()
            throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isForbidden())
                .andReturn();

        mockMvc.perform(put("/users/1/role"))
                .andExpect(status().isForbidden())
                .andReturn();

        mockMvc.perform(put("/users/me"))
                .andExpect(status().isForbidden())
                .andReturn();

        mockMvc.perform(put("/users/me/password"))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    private JsonNode getErrorResponse(MvcResult mvcResult) {
        try {
            return objectMapper.readTree(mvcResult.getResponse()
                    .getContentAsString()).get("errors");
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
