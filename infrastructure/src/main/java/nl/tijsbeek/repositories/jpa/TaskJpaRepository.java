package nl.tijsbeek.repositories.jpa;

import nl.tijsbeek.entities.TaskJPA;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskJpaRepository
        extends JpaRepository<TaskJPA, Long> {
}