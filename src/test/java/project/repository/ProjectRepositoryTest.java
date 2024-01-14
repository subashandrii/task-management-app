package project.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import project.model.Project;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProjectRepositoryTest {
    @Autowired
    private ProjectRepository projectRepository;

    @Test
    @DisplayName("Find projects by user id when user has projects")
    @Sql(scripts = {
            "classpath:database/add-users-to-table.sql",
            "classpath:database/add-projects-to-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/remove-all-tables.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findProjectsByUserId_UserHasProjects_ReturnsFourProjects() {
        Long userId = 1L;
        List<Project> projects = projectRepository.findAllByUserIdWithSorting(
                userId, PageRequest.of(0, 10));

        assertEquals(projects.size(), 4);
        assertEquals(projects.get(0).getId(), 5L);
        assertEquals(projects.get(1).getId(), 3L);
        assertEquals(projects.get(2).getId(), 1L);
        assertEquals(projects.get(3).getId(), 2L);
    }

    @Test
    @DisplayName("Find projects by user id when user does not have projects")
    @Sql(scripts = {
            "classpath:database/add-users-to-table.sql",
            "classpath:database/add-projects-to-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/remove-all-tables.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findProjectsByUserId_UserDoesNotHaveProjects_ReturnsEmptyList() {
        Long userId = 3L;
        List<Project> projects = projectRepository.findAllByUserIdWithSorting(
                userId, PageRequest.of(0, 10));

        assertTrue(projects.isEmpty());
    }
}
