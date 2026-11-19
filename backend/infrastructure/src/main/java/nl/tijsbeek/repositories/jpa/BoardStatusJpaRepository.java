package nl.tijsbeek.repositories.jpa;

import nl.tijsbeek.entities.BoardStatusJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardStatusJpaRepository
        extends JpaRepository<BoardStatusJPA, Long> {

    List<BoardStatusJPA> findAllByBoardId(long boardId);

    Optional<BoardStatusJPA> findByIdAndBoardId(long statusId, long boardId);
}
