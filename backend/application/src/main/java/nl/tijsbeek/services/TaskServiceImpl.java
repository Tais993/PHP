package nl.tijsbeek.services;

import nl.tijsbeek.entities.Board;
import nl.tijsbeek.entities.BoardStatus;
import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.entities.Task;
import nl.tijsbeek.exceptions.TaskNotFoundException;
import nl.tijsbeek.exceptions.BoardViewNotFoundException;
import nl.tijsbeek.exceptions.InvalidTaskReferenceException;
import nl.tijsbeek.interfaces.repositories.BoardRepository;
import nl.tijsbeek.interfaces.repositories.BoardStatusRepository;
import nl.tijsbeek.interfaces.repositories.BoardViewRepository;
import nl.tijsbeek.interfaces.repositories.TaskRepository;
import nl.tijsbeek.interfaces.services.TaskGroupingService;
import nl.tijsbeek.interfaces.services.TaskService;
import nl.tijsbeek.results.TaskViewResult;
import org.jetbrains.annotations.Contract;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final BoardViewRepository boardViewRepository;
    private final BoardRepository boardRepository;
    private final BoardStatusRepository boardStatusRepository;
    private final TaskGroupingService taskGroupingService;

    @Contract(pure = true)
    public TaskServiceImpl(TaskRepository taskRepository, BoardViewRepository boardViewRepository, BoardRepository boardRepository, BoardStatusRepository boardStatusRepository, TaskGroupingService taskGroupingService) {
        this.taskRepository = taskRepository;
        this.boardViewRepository = boardViewRepository;
        this.boardRepository = boardRepository;
        this.boardStatusRepository = boardStatusRepository;
        this.taskGroupingService = taskGroupingService;
    }

    @Transactional
    @Override
    public Task createTask(String title, Long boardId, String description, Long statusId) {
        long selectedBoardId = boardId == null ? 1L : boardId;
        Board board = boardRepository.findById(selectedBoardId)
                .orElseThrow(() -> new InvalidTaskReferenceException(
                        "Board with ID " + selectedBoardId + " was not found"));

        BoardStatus status = null;
        if (statusId != null) {
            status = board.getStatuses().stream()
                    .filter(option -> statusId.equals(option.getId()))
                    .findFirst()
                    .orElseThrow(() -> new InvalidTaskReferenceException(
                            "Status does not exist on the selected board"));
        }

        Task task = new Task(null, selectedBoardId, title, description, null, status);


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

    @Override
    public TaskViewResult getTasksOfBoardView(long boardViewId) {
        BoardView view = boardViewRepository.findById(boardViewId)
                .orElseThrow(() -> new BoardViewNotFoundException(boardViewId));

        List<Task> tasks = taskRepository.findAllForView(boardViewId);
        List<BoardStatus> boardStatuses = boardStatusRepository.findAllByBoardId(view.getBoardId());

        return switch (view.getGroupBy()) {
            case STATUS -> taskGroupingService.groupByStatus(tasks, boardStatuses);
        };
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
