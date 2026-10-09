package nl.tijsbeek.services;

import nl.tijsbeek.entities.Task;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface TaskService {

    @Transactional
    Task createTask(String title, String description, String status);

    @Transactional
    void deleteTask(long id);

    @Transactional(readOnly = true)
    Optional<Task> getTask(long id);

    @Transactional
    Task changeTaskTitle(long id, String newTitle);
}
