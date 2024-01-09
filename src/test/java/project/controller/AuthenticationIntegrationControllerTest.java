package project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
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
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;
import project.dto.user.request.UserLoginRequestDto;
import project.dto.user.request.UserRegistrationRequestDto;
import project.dto.user.response.UserResponseDto;
import project.dto.user.response.UserTokenResponseDto;
import project.secure.JwtUtil;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationIntegrationControllerTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtUtil jwtUtil;
    private UserRegistrationRequestDto userRegistrationRequestDto;
    private UserResponseDto userResponseDto;
    private UserLoginRequestDto userLoginRequestDto;
    
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
        userRegistrationRequestDto = new UserRegistrationRequestDto()
                                             .setUsername("bob123")
                                             .setEmail("bob1@email.com")
                                             .setFirstName("Bob")
                                             .setLastName("Lastname")
                                             .setPassword("Password123!!!")
                                             .setRepeatPassword("Password123!!!");
        
        userResponseDto = new UserResponseDto()
                                              .setId(1L)
                                              .setUsername("bob123")
                                              .setFirstName("Bob")
                                              .setLastName("Lastname")
                                              .setRole("USER")
                                              .setEmail("bob@email.com");
        
        userLoginRequestDto = new UserLoginRequestDto()
                                      .setEmailOrUsername("bob123")
                                      .setPassword("Password123!!!");
    }
    
    @SneakyThrows
    private static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/remove-users-from-table.sql"));
        }
    }
    
    @AfterEach
    void afterEach(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }
    
    @Test
    @DisplayName("Registration of a new user with admin role by valid dto")
    public void registerForAdmin_ValidRequestDto_ReturnsResponseDto() throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(userRegistrationRequestDto);
        
        MvcResult result = mockMvc.perform(
                        post("/auth/register/admin")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                                   .andExpect(status().isCreated())
                                   .andReturn();
        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserResponseDto.class);
         
        assertNotNull(actual);
        EqualsBuilder.reflectionEquals(userResponseDto, actual, "id");
    }
    
    @Test
    @DisplayName("Registration of a new user by valid dto")
    public void register_ValidRequestDto_ReturnsResponseDto() throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(userRegistrationRequestDto);
        
        MvcResult result = mockMvc.perform(
                        post("/auth/register")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                                   .andExpect(status().isCreated())
                                   .andReturn();
        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserResponseDto.class);
        
        assertNotNull(actual);
        EqualsBuilder.reflectionEquals(userResponseDto, actual, "id");
    }
    
    @Test
    @DisplayName("User log in by valid dto")
    public void login_ValidRequestDto_ReturnsResponseDto() throws Exception {
        String jsonRegistrationRequestDto =
                objectMapper.writeValueAsString(userRegistrationRequestDto);
        mockMvc.perform(post("/auth/register")
                        .content(jsonRegistrationRequestDto)
                        .contentType(MediaType.APPLICATION_JSON));
        
        String jsonLoginRequestDto = objectMapper.writeValueAsString(userLoginRequestDto);
        MvcResult result = mockMvc.perform(
                        post("/auth/login")
                                .content(jsonLoginRequestDto)
                                .contentType(MediaType.APPLICATION_JSON))
                                   .andExpect(status().isOk())
                                   .andReturn();
        UserTokenResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserTokenResponseDto.class);
        
        assertNotNull(actual);
        assertTrue(jwtUtil.isValidToken(actual.getToken()));
    }
    
    @Test
    @DisplayName("Registration of a new user by not valid dto")
    public void registerForAdminAndUser_NotValidRequestDto_ReturnsException() throws Exception {
        UserRegistrationRequestDto notValidRegistrationDto = new UserRegistrationRequestDto()
                                                                     .setUsername("b")
                                                                     .setEmail("bob")
                                                                     .setFirstName("123")
                                                                     .setLastName("234")
                                                                     .setPassword("password")
                                                                     .setRepeatPassword("pasword");
        String jsonNotValidRequest = objectMapper.writeValueAsString(notValidRegistrationDto);
        
        MvcResult resultFromRegisterAdminEndpoint = mockMvc.perform(
                        post("/auth/register/admin")
                                .content(jsonNotValidRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                                   .andExpect(status().isBadRequest())
                                   .andReturn();
        MvcResult resultFromRegisterEndpoint = mockMvc.perform(
                        post("/auth/register")
                                .content(jsonNotValidRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                                    .andExpect(status().isBadRequest())
                                    .andReturn();
        List<String> errorMessages =
                Stream.of(resultFromRegisterAdminEndpoint, resultFromRegisterEndpoint)
                        .map(this::getErrorResponse)
                        .map(messages -> objectMapper.convertValue(messages, String[].class))
                        .flatMap(Arrays::stream)
                        .toList();
        
        assertEquals(12, errorMessages.size());
        assertTrue(errorMessages.contains("email format is not valid"));
        assertTrue(errorMessages.contains("username format is not valid"));
        assertTrue(errorMessages.contains("passwords do not match!"));
        assertTrue(errorMessages.contains("password format is not valid"));
        assertTrue(errorMessages.contains("firstName format is not valid"));
        assertTrue(errorMessages.contains("lastName format is not valid"));
    }
    
    @Test
    @DisplayName("User log in by not valid dto")
    public void login_NotValidRequestDto_ReturnsException() throws Exception {
        UserLoginRequestDto notValidLoginDto = new UserLoginRequestDto();
        String jsonNotValidRequest = objectMapper.writeValueAsString(notValidLoginDto);
        
        MvcResult resultFromLoginEndpoint = mockMvc.perform(
                        post("/auth/login")
                                .content(jsonNotValidRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                                                            .andExpect(status().isBadRequest())
                                                            .andReturn();
        List<String> errorMessages = objectMapper.convertValue(
                getErrorResponse(resultFromLoginEndpoint), List.class);
        
        assertEquals(2, errorMessages.size());
        assertTrue(errorMessages.contains("emailOrUsername must not be null"));
        assertTrue(errorMessages.contains("password must not be null"));
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
