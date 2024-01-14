package project.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import project.config.MapperConfig;
import project.dto.project.ProjectRequestDto;
import project.dto.project.ProjectResponseDto;
import project.model.Project;

@Mapper(config = MapperConfig.class)
public interface ProjectMapper {
    Project toModel(ProjectRequestDto requestDto);

    @Mapping(source = "user.id", target = "userId")
    ProjectResponseDto toDto(Project project);
}
