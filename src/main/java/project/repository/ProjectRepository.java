package project.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import project.model.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("FROM Project p WHERE p.user.id = ?1 ORDER BY CASE p.status "
            + "WHEN 'INITIATED' THEN 1 "
            + "WHEN 'IN_PROGRESS' THEN 2 "
            + "WHEN 'COMPLETED' THEN 3 "
            + "ELSE 4 END, p.id DESC")
    List<Project> findAllByUserIdWithSorting(Long id, Pageable pageable);
}
