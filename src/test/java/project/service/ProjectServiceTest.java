package project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;
import project.dto.project.ProjectRequestDto;
import project.dto.project.ProjectResponseDto;
import project.exception.AccessDeniedException;
import project.mapper.ProjectMapper;
import project.model.Project;
import project.model.User;
import project.repository.ProjectRepository;
import project.repository.UserRepository;
import project.service.impl.ProjectServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectMapper projectMapper;
    @InjectMocks
    private ProjectServiceImpl projectService;
    private ProjectRequestDto projectRequestDto;
    private ProjectRequestDto updatedProjectRequestDto;
    private ProjectResponseDto projectResponseDto;
    private Project project;
    private User user;

    @BeforeEach
    public void setUp() {
        user = new User()
                .setId(1L)
                .setUsername("bob123");

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
                .setUser(user);
    }

    @Test
    @DisplayName("Create a new project")
    public void createProject_DataIsCorrect_ReturnsResponseDto() {
        String username = "bob123";

        when(userRepository.getUserByUsername(username)).thenReturn(user);
        when(projectMapper.toModel(projectRequestDto)).thenReturn(project);
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toDto(project)).thenReturn(projectResponseDto);
        ProjectResponseDto actual = projectService.create(projectRequestDto, username);

        assertNotNull(actual);
        EqualsBuilder.reflectionEquals(projectResponseDto, actual);
    }

    @Test
    @DisplayName("Get list of projects by username")
    public void getAllProjects_ReturnsListOfOneResponseDto() {
        String username = "bob123";

        when(userRepository.getUserByUsername(username)).thenReturn(user);
        when(projectRepository.findAllByUserIdWithSorting(user.getId(),
                PageRequest.of(0, 10))).thenReturn(List.of(project));
        when(projectMapper.toDto(project)).thenReturn(projectResponseDto);
        List<ProjectResponseDto> actual = projectService.getAll(
                username, PageRequest.of(0, 10));

        assertEquals(1, actual.size());
        EqualsBuilder.reflectionEquals(projectResponseDto, actual.get(0));
    }

    @Test
    @DisplayName("Get project by id")
    public void getProject_IdIsCorrect_ReturnsResponseDto() {
        Long id = 1L;
        String username = "bob123";

        when(projectRepository.findById(id)).thenReturn(Optional.of(project));
        when(projectMapper.toDto(project)).thenReturn(projectResponseDto);
        ProjectResponseDto actual = projectService.get(id, username);

        assertNotNull(actual);
        EqualsBuilder.reflectionEquals(projectResponseDto, actual);
    }

    @Test
    @DisplayName("Update project by id with correct data")
    public void updateProject_DataIsCorrect_ReturnsResponseDto() {
        Long id = 1L;
        String username = "bob123";
        final ProjectResponseDto expected = new ProjectResponseDto()
                .setId(1L)
                .setName("project111")
                .setStartDate(LocalDate.now().plusDays(4))
                .setEndDate(LocalDate.now().plusDays(5))
                .setUserId(1L)
                .setStatus("INITIATED");

        when(projectRepository.findById(id)).thenReturn(Optional.of(project));
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toDto(project)).thenReturn(projectResponseDto);
        ProjectResponseDto actual = projectService.update(id, updatedProjectRequestDto, username);

        assertNotNull(actual);
        EqualsBuilder.reflectionEquals(expected, actual);
    }

    @Test
    @DisplayName("Delete project by id")
    public void deleteProject_IdIsCorrect_Success() {
        Long id = 1L;
        String username = "bob123";

        when(projectRepository.findById(id)).thenReturn(Optional.of(project));
        projectService.delete(id, username);

        verify(projectRepository, times(1)).delete(project);
    }

    @Test
    @DisplayName("Testing method 'check project by id' in different methods "
            + "when no project with that id")
    public void checkProjectById_NoProjectWithThatId_ReturnsExceptions() {
        Long id = 100L;
        String username = "bob123";

        when(projectRepository.findById(id)).thenReturn(Optional.empty());
        Exception exceptionInGetMethod = assertThrows(EntityNotFoundException.class,
                () -> projectService.get(id, username));
        Exception exceptionInUpdateMethod = assertThrows(EntityNotFoundException.class,
                () -> projectService.update(id, updatedProjectRequestDto, username));
        Exception exceptionInDeleteMethod = assertThrows(EntityNotFoundException.class,
                () -> projectService.get(id, username));

        Stream.of(exceptionInGetMethod, exceptionInUpdateMethod, exceptionInDeleteMethod)
                        .forEach(exception -> assertEquals("Project with id "
                                + id + " is not found", exception.getMessage()));
    }

    @Test
    @DisplayName("Testing method 'check project by id' in different methods "
            + "when user wants to access another user's project")
    public void checkProjectById_UserGetsAccessToAnotherUsersProject_ReturnsExceptions() {
        Long id = 1L;
        String username = "alice123";

        when(projectRepository.findById(id)).thenReturn(Optional.of(project));
        Exception exceptionInGetMethod = assertThrows(AccessDeniedException.class,
                () -> projectService.get(id, username));
        Exception exceptionInUpdateMethod = assertThrows(AccessDeniedException.class,
                () -> projectService.update(id, updatedProjectRequestDto, username));
        Exception exceptionInDeleteMethod = assertThrows(AccessDeniedException.class,
                () -> projectService.get(id, username));

        Stream.of(exceptionInGetMethod, exceptionInUpdateMethod, exceptionInDeleteMethod)
                .forEach(exception -> assertEquals("You do not have a project with id "
                        + id, exception.getMessage()));
    }
}
