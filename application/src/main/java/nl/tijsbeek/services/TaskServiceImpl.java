package nl.tijsbeek.services;

import nl.tijsbeek.entities.Task;
import nl.tijsbeek.repositories.TaskRepository;
import org.jetbrains.annotations.Contract;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Contract(pure = true)
    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    @Override
    public Task createTask(String title, String description, String status) {
        Task task = new Task(null, title, description, null,status);

        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    @Override
    public Task getTask(long id) {
        return taskRepository.findById(id)
                .orElseThrow();
    }

    @Transactional
    @Override
    public Task changeTaskTitle(long id, String newTitle) {
        Task task = taskRepository.findById(id)
                .orElseThrow();

        task.changeTitle(newTitle);

        return taskRepository.save(task);
    }
}