package nl.tijsbeek.repositories.jpa;

import nl.tijsbeek.entities.BoardJPA;
import nl.tijsbeek.entities.TaskJPA;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardJpaRepository
        extends JpaRepository<BoardJPA, Long> {
}