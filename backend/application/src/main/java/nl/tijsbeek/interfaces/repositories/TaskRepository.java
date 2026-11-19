package nl.tijsbeek.interfaces.repositories;

import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.entities.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {

    Optional<Task> findById(long id);

    List<Task> findAll();

    List<Task> findAllByBoardId(long boardId);

    List<Task> findAllForView(long boardViewId);
    List<Task> findAllForView(BoardView view);

    Task save(Task task);

    void deleteById(long id);
}
