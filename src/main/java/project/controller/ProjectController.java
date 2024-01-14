package project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import project.dto.project.ProjectRequestDto;
import project.dto.project.ProjectResponseDto;
import project.service.ProjectService;

@Tag(name = "Projects management")
@RequiredArgsConstructor
@RestController
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Create a new project")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponseDto create(@RequestBody @Valid
                                         ProjectRequestDto projectRequestDto,
                                     Authentication authentication) {
        return projectService.create(projectRequestDto, authentication.getName());
    }

    @GetMapping
    @Operation(summary = "Get user's projects")
    @ResponseStatus(HttpStatus.OK)
    public List<ProjectResponseDto> getAll(Authentication authentication,
                                           Pageable pageable) {
        return projectService.getAll(authentication.getName(), pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project details by id")
    @ResponseStatus(HttpStatus.OK)
    public ProjectResponseDto get(@PathVariable Long id,
                                  Authentication authentication) {
        return projectService.get(id, authentication.getName());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project")
    @ResponseStatus(HttpStatus.OK)
    public ProjectResponseDto update(@PathVariable Long id,
                                     @RequestBody @Valid ProjectRequestDto projectRequestDto,
                                     Authentication authentication) {
        return projectService.update(id, projectRequestDto, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                                     Authentication authentication) {
        projectService.delete(id, authentication.getName());
    }
}
