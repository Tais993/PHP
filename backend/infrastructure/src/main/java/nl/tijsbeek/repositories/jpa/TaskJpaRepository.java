package nl.tijsbeek.repositories.jpa;

import nl.tijsbeek.entities.TaskJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskJpaRepository
        extends JpaRepository<TaskJPA, Long> {

    List<TaskJPA> findAllByBoardId(long boardId);
}