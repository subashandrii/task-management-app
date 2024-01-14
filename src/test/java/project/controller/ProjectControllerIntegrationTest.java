package project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.time.LocalDate;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;
import project.dto.project.ProjectRequestDto;
import project.dto.project.ProjectResponseDto;
import project.model.Project;
import project.model.User;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProjectControllerIntegrationTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private ProjectRequestDto projectRequestDto;
    private ProjectRequestDto updatedProjectRequestDto;
    private ProjectResponseDto projectResponseDto;
    private Project project;

    @BeforeAll
    static void beforeAll(@Autowired DataSource dataSource,
                          @Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        teardown(dataSource);
    }

    @SneakyThrows
    private static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/remove-all-tables.sql"));
        }
    }

    @BeforeEach
    public void setUp() {
        projectRequestDto = new ProjectRequestDto()
                .setName("project")
                .setDescription("description")
                .setStartDate(LocalDate.now())
                .setEndDate(LocalDate.now().plusDays(3));

        updatedProjectRequestDto = new ProjectRequestDto()
                .setName("project111")
                .setStartDate(LocalDate.now().plusDays(4))
                .setEndDate(LocalDate.now().plusDays(5));

        projectResponseDto = new ProjectResponseDto()
                .setId(1L)
                .setName("project")
                .setDescription("description")
                .setStartDate(LocalDate.now())
                .setEndDate(LocalDate.now().plusDays(3))
                .setUserId(1L)
                .setStatus("INITIATED");

        project = new Project()
                .setId(1L)
                .setName("project")
                .setDescription("description")
                .setStartDate(LocalDate.now())
                .setEndDate(LocalDate.now().plusDays(3))
                .setUser(new User()
                        .setId(1L)
                        .setUsername("bob123"));
    }

    @AfterEach
    void afterEach(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @Test
    @DisplayName("Create a new project by valid dto")
    @WithMockUser(username = "bob123")
    @Sql(scripts = {
            "classpath:database/add-users-to-table.sql",
            "classpath:database/add-projects-to-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void createProject_DtoIsValid_ReturnsResponseDto() throws Exception {
        String jsonRequest =
                objectMapper.writeValueAsString(projectRequestDto);

        MvcResult result = mockMvc.perform(
                        post("/projects")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        ProjectResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), ProjectResponseDto.class);

        assertNotNull(actual);
        EqualsBuilder.reflectionEquals(projectResponseDto.setId(6L), actual);
    }

    @Test
    @DisplayName("Get user's projects")
    @WithMockUser(username = "bob123")
    @Sql(scripts = {
            "classpath:database/add-users-to-table.sql",
            "classpath:database/add-projects-to-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void getAllProjects_ReturnsFourResponseDto() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/projects"))
                .andExpect(status().isOk())
                .andReturn();
        Project[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), Project[].class);

        assertEquals(actual.length, 4);
        assertEquals(actual[0].getId(), 5L);
        assertEquals(actual[1].getId(), 3L);
        assertEquals(actual[2].getId(), 1L);
        assertEquals(actual[3].getId(), 2L);
    }

    @Test
    @DisplayName("Get user's project by id")
    @WithMockUser(username = "bob123")
    @Sql(scripts = {
            "classpath:database/add-users-to-table.sql",
            "classpath:database/add-projects-to-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void getProject_IdIsCorrect_ReturnsResponseDto() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/projects/5"))
                .andExpect(status().isOk())
                 .andReturn();
        ProjectResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), ProjectResponseDto.class);

        assertNotNull(actual);
        assertEquals(5L, actual.getId());
        assertEquals("project5", actual.getName());
    }

    @Test
    @DisplayName("Update project by id with valid dto")
    @WithMockUser(username = "bob123")
    @Sql(scripts = {
            "classpath:database/add-users-to-table.sql",
            "classpath:database/add-projects-to-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void updateProject_DataIsCorrect_ReturnsResponseDto() throws Exception {
        ProjectResponseDto expected = projectResponseDto
                .setName("project111")
                .setStartDate(LocalDate.now().plusDays(4))
                .setEndDate(LocalDate.now().plusDays(5));
        String jsonRequest =
                objectMapper.writeValueAsString(updatedProjectRequestDto);

        MvcResult result = mockMvc.perform(
                        put("/projects/1")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ProjectResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), ProjectResponseDto.class);

        assertNotNull(actual);
        EqualsBuilder.reflectionEquals(expected, actual);
    }

    @Test
    @DisplayName("Delete project by id")
    @WithMockUser(username = "bob123")
    @Sql(scripts = {
            "classpath:database/add-users-to-table.sql",
            "classpath:database/add-projects-to-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void deleteProject_IdIsCorrect_Success() throws Exception {
        mockMvc.perform(delete("/projects/1"))
                .andExpect(status().isNoContent());

        MvcResult result = mockMvc.perform(get("/projects")).andReturn();
        ProjectResponseDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), ProjectResponseDto[].class);
        Arrays.stream(actual).forEach(project -> assertNotEquals(1L, project.getId()));
    }

    @Test
    @DisplayName("Testing methods for processing not valid dto")
    @WithMockUser(username = "bob123")
    @Sql(scripts = {
            "classpath:database/add-users-to-table.sql",
            "classpath:database/add-projects-to-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testMethodsForProcessingNotValidDto_ReturnsException() throws Exception {
        ProjectRequestDto emptyDto = new ProjectRequestDto();
        ProjectRequestDto notValidDataInDto = new ProjectRequestDto()
                .setName(String.valueOf(new char[51]))
                .setDescription(String.valueOf(new char[255]))
                .setStartDate(LocalDate.now().minusDays(1))
                .setEndDate(LocalDate.now().minusDays(3));
        String emptyDtoJsonRequest =
                objectMapper.writeValueAsString(emptyDto);
        String notValidDataInDtoJsonRequest =
                objectMapper.writeValueAsString(notValidDataInDto);

        MvcResult resulOfEmptyDto = mockMvc.perform(post("/projects")
                        .content(emptyDtoJsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        MvcResult resultOfNotValidDataInDto = mockMvc.perform(put("/projects/1")
                        .content(notValidDataInDtoJsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        List<String> errorMessages =
                Stream.of(resulOfEmptyDto, resultOfNotValidDataInDto)
                        .map(this::getErrorResponse)
                        .map(messages -> objectMapper.convertValue(messages, String[].class))
                        .flatMap(Arrays::stream)
                        .toList();

        assertEquals(8, errorMessages.size());
        assertTrue(errorMessages.contains("startDate must be before endDate"));
        assertTrue(errorMessages.contains("name must not be null"));
        assertTrue(errorMessages.contains("startDate must not be null"));
        assertTrue(errorMessages.contains("endDate must not be null"));
        assertTrue(errorMessages.contains("name size must be between 2 and 50"));
        assertTrue(errorMessages.contains("description size must be between 0 and 254"));
        assertTrue(errorMessages.contains("startDate must be a date in the "
                + "present or in the future"));
        assertTrue(errorMessages.contains("endDate must be a date in the "
                + "present or in the future"));
    }

    @Test
    @DisplayName("Unauthorized user attempts to access endpoints intended for authorized users")
    @Sql(scripts = {
            "classpath:database/add-users-to-table.sql",
            "classpath:database/add-projects-to-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void unauthorizedUserGoesToEndpointsForAuthorizedUsers_ReturnsException()
            throws Exception {
        String jsonRequest =
                objectMapper.writeValueAsString(projectRequestDto);

        mockMvc.perform(post("/projects")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/projects"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/projects/1")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/projects/1"))
                .andExpect(status().isForbidden());
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
