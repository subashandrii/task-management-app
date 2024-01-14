package project.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import project.dto.project.ProjectRequestDto;
import project.dto.project.ProjectResponseDto;
import project.exception.AccessDeniedException;
import project.mapper.ProjectMapper;
import project.model.Project;
import project.model.User;
import project.repository.ProjectRepository;
import project.repository.UserRepository;
import project.service.ProjectService;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    @Override
    @Transactional
    public ProjectResponseDto create(ProjectRequestDto projectRequestDto, String username) {
        User user = userRepository.getUserByUsername(username);
        Project project = projectRepository.save(
                projectMapper.toModel(projectRequestDto)
                        .setUser(user));
        log.info("User ({}) created a new project {}", username, project);
        return projectMapper.toDto(project);
    }

    @Override
    public List<ProjectResponseDto> getAll(String username, Pageable pageable) {
        User user = userRepository.getUserByUsername(username);
        return projectRepository.findAllByUserIdWithSorting(user.getId(), pageable).stream()
                .map(projectMapper::toDto)
                .toList();
    }

    @Override
    public ProjectResponseDto get(Long id, String username) {
        Project project = checkProjectById(id, username);
        return projectMapper.toDto(project);
    }

    @Override
    @Transactional
    public ProjectResponseDto update(Long id,
                                     ProjectRequestDto projectRequestDto,
                                     String username) {
        Project project = checkProjectById(id, username);
        project.setName(projectRequestDto.getName())
                .setDescription(projectRequestDto.getDescription())
                .setStartDate(projectRequestDto.getStartDate())
                .setEndDate(projectRequestDto.getEndDate());
        log.info("User ({}) updated a project {}", username, project);
        return projectMapper.toDto(projectRepository.save(project));
    }

    @Override
    @Transactional
    public void delete(Long id, String username) {
        Project project = checkProjectById(id, username);
        log.info("User ({}) deleted a project (ID {})", username, id);
        projectRepository.delete(project);
    }

    private Project checkProjectById(Long id, String username) {
        Project project = projectRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Project with id " + id + " is not found"));
        if (!project.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have a project with id " + id);
        }
        return project;
    }
}
