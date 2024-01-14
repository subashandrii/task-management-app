package project.dto.project;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;
import lombok.experimental.Accessors;
import project.validation.annotation.StartDateBeforeEndDate;

@Data
@Accessors(chain = true)
@StartDateBeforeEndDate(
        startDateField = "startDate",
        endDateField = "endDate"
)
public class ProjectRequestDto {
    @NotNull
    @Size(min = 2, max = 50)
    private String name;
    @Size(max = 254)
    private String description;
    @NotNull
    @FutureOrPresent
    private LocalDate startDate;
    @NotNull
    @FutureOrPresent
    private LocalDate endDate;
}
