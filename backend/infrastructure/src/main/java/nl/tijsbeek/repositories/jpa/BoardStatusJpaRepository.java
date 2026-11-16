package nl.tijsbeek.repositories.jpa;

import nl.tijsbeek.entities.BoardStatusJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardStatusJpaRepository
        extends JpaRepository<BoardStatusJPA, Long> {

    List<BoardStatusJPA> findAllByBoardId(long boardId);
}
