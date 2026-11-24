package nl.tijsbeek.interfaces.services;

import nl.tijsbeek.entities.Task;
import nl.tijsbeek.results.TaskViewResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskService {

    @Transactional
    Task createTask(String title, Long boardId, String description, Long statusId);

    @Transactional(readOnly = true)
    Optional<Task> getTask(long id);

    @Transactional(readOnly = true)
    List<Task> getAllTasks();

    @Transactional(readOnly = true)
    List<Task> getAllTasksByBoardId(long boardId);

    @Transactional(readOnly = true)
    TaskViewResult getTasksOfBoardView(long boardId);

    @Transactional
    Task updateTask(long id, FieldUpdate<String> title, FieldUpdate<String> description, FieldUpdate<LocalDateTime> dueDate, FieldUpdate<Long> statusId);

    @Transactional
    void deleteTask(long id);
}
