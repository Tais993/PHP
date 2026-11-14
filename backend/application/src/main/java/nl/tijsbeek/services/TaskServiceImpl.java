package nl.tijsbeek.services;

import nl.tijsbeek.entities.Task;
import nl.tijsbeek.entities.BoardStatus;
import nl.tijsbeek.interfaces.repositories.BoardRepository;
import nl.tijsbeek.exceptions.TaskNotFoundException;
import nl.tijsbeek.interfaces.repositories.TaskRepository;
import nl.tijsbeek.interfaces.services.TaskService;
import org.jetbrains.annotations.Contract;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final BoardRepository boardRepository;

    @Contract(pure = true)
    public TaskServiceImpl(TaskRepository taskRepository, BoardRepository boardRepository) {
        this.taskRepository = taskRepository;
        this.boardRepository = boardRepository;
    }

    @Transactional
    @Override
    public Task createTask(String title, Long boardId, String description, Long statusId) {
        // TODO automatically add it to default board
        // if board ID is null use the default one
        if (boardId == null) {
            boardId = 1L;
        }
        // TODO add actual logic so the default one is opened, but that requires users so it's w/e for now

        BoardStatus status = null;
        if (statusId != null) {
            status = boardRepository.findById(boardId)
                    .orElseThrow(() -> new IllegalArgumentException("Board was not found"))
                    .getStatuses().stream()
                    .filter(option -> statusId.equals(option.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Status does not belong to this board"));
        }

        Task task = new Task(null, boardId, title, description, null, status);


        return taskRepository.save(task);
    }

    @Transactional
    @Override
    public void deleteTask(long id) {
        taskRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Task> getTask(long id) {
        return taskRepository.findById(id);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public List<Task> getAllTasksByBoardId(long boardId) {
        return taskRepository.findAllByBoardId(boardId);
    }

    @Transactional
    @Override
    public Task changeTaskTitle(long id, String newTitle) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        task.changeTitle(newTitle);

        return taskRepository.save(task);
    }
}
