package nl.tijsbeek.repositories.jpa;

import nl.tijsbeek.entities.BoardViewJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardViewJpaRepository
        extends JpaRepository<BoardViewJPA, Long> {

    List<BoardViewJPA> findAllByBoardIdOrderByPositionAscIdAsc(long boardId);
}
