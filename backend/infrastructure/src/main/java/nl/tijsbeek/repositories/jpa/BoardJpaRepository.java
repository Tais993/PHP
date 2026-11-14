package nl.tijsbeek.repositories.jpa;

import nl.tijsbeek.entities.BoardJPA;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardJpaRepository
        extends JpaRepository<BoardJPA, Long> {
}