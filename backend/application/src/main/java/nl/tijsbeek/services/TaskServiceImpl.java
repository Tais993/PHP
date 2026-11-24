package nl.tijsbeek.services;

import nl.tijsbeek.entities.BoardStatus;
import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.entities.Task;
import nl.tijsbeek.exceptions.BoardViewNotFoundException;
import nl.tijsbeek.exceptions.InvalidTaskReferenceException;
import nl.tijsbeek.exceptions.TaskNotFoundException;
import nl.tijsbeek.interfaces.repositories.BoardRepository;
import nl.tijsbeek.interfaces.repositories.BoardStatusRepository;
import nl.tijsbeek.interfaces.repositories.BoardViewRepository;
import nl.tijsbeek.interfaces.repositories.TaskRepository;
import nl.tijsbeek.interfaces.services.FieldUpdate;
import nl.tijsbeek.interfaces.services.TaskGroupingService;
import nl.tijsbeek.interfaces.services.TaskService;
import nl.tijsbeek.results.TaskViewResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final BoardViewRepository boardViewRepository;
    private final BoardRepository boardRepository;
    private final BoardStatusRepository boardStatusRepository;
    private final TaskGroupingService taskGroupingService;

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

        if (!boardRepository.existsById(selectedBoardId)) {
            throw new InvalidTaskReferenceException("Board with ID " + selectedBoardId + " was not found");
        }

        BoardStatus status = null;
        if (statusId != null) {
            status = boardStatusRepository.findByIdAndboardId(statusId, selectedBoardId)
                    .orElseThrow(() -> new InvalidTaskReferenceException(
                            "Status does not exist on the selected board"));
        }

        Task task = new Task(selectedBoardId, title, description, null, status);
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

        List<Task> tasks = taskRepository.findAllForView(view);
        List<BoardStatus> boardStatuses = boardStatusRepository.findAllByBoardId(view.getBoardId());

        return switch (view.getGroupBy()) {
            case STATUS -> taskGroupingService.groupByStatus(tasks, boardStatuses);
        };
    }

    @Override
    public Task updateTask(
            long taskId,
            FieldUpdate<String> title,
            FieldUpdate<String> description,
            FieldUpdate<LocalDateTime> dueDate,
            FieldUpdate<Long> statusId
    ) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        if (title.supplied()) {
            task.setTitle(title.value());
        }

        if (description.supplied()) {
            task.setDescription(description.value());
        }

        if (dueDate.supplied()) {
            task.setDueDate(dueDate.value());
        }

        if (statusId.supplied()) {
            Long requestedStatusId = statusId.value();
            BoardStatus status = null;
            if (requestedStatusId != null) {
                status = boardStatusRepository.findByIdAndboardId(requestedStatusId, task.getBoardId())
                        .orElseThrow(() -> new InvalidTaskReferenceException(
                                "Status does not exist on the selected board"));
            }

            task.setStatus(status);
        }

        return taskRepository.save(task);
    }
}
