package project.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import project.dto.project.ProjectRequestDto;
import project.dto.project.ProjectResponseDto;

public interface ProjectService {
    ProjectResponseDto create(ProjectRequestDto projectRequestDto, String username);

    List<ProjectResponseDto> getAll(String username, Pageable pageable);

    ProjectResponseDto get(Long id, String username);

    ProjectResponseDto update(Long id, ProjectRequestDto projectRequestDto, String username);

    void delete(Long id, String username);
}
