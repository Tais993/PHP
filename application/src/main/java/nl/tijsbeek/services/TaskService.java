package nl.tijsbeek.services;

import nl.tijsbeek.entities.Task;
import org.springframework.transaction.annotation.Transactional;

public interface TaskService {

    @Transactional
    Task createTask(String title, String description, String status);

    @Transactional(readOnly = true)
    Task getTask(long id);

    @Transactional
    Task changeTaskTitle(long id, String newTitle);
}
