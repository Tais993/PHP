package nl.tijsbeek.services;

import nl.tijsbeek.entities.BoardStatus;
import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.entities.Task;
import nl.tijsbeek.exceptions.TaskNotFoundException;
import nl.tijsbeek.interfaces.repositories.BoardRepository;
import nl.tijsbeek.interfaces.repositories.BoardViewRepository;
import nl.tijsbeek.interfaces.repositories.TaskRepository;
import nl.tijsbeek.interfaces.services.TaskService;
import nl.tijsbeek.results.TaskViewResult;
import org.jetbrains.annotations.Contract;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final BoardViewRepository boardViewRepository;
    private final BoardRepository boardRepository;

    @Contract(pure = true)
    public TaskServiceImpl(TaskRepository taskRepository, BoardViewRepository boardViewRepository, BoardRepository boardRepository) {
        this.taskRepository = taskRepository;
        this.boardViewRepository = boardViewRepository;
        this.boardRepository = boardRepository;
    }

    @Transactional
    @Override
    public Task createTask(String title, Long boardId, String description, Long statusId) {
        // if board ID is null use the default one
        if (boardId == null) {
            boardId = 1L;
        }

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

    @Override
    public TaskViewResult getTasksOfBoardView(long boardViewId) {
        BoardView view = boardViewRepository.findById(boardViewId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Board view was not found"));

        List<Task> tasks = taskRepository.findAllForView(boardViewId);

        return switch (view.getGroupBy()) {
            case STATUS -> groupTasks(tasks, BoardView.GroupBy.STATUS, task -> {
                BoardStatus status = task.getStatus();

                return status == null
                        ? new GroupKey(null, "No status", 0)
                        : new GroupKey(status.getId(), status.getName(), status.getPosition());
            });
        };
    }


    private record GroupKey(Long id, String name, int position) {}

    private TaskViewResult groupTasks(
            List<Task> tasks,
            BoardView.GroupBy groupBy,
            Function<Task, GroupKey> keyExtractor
    ) {
        Map<GroupKey, List<Task>> grouped = new LinkedHashMap<>();

        tasks.forEach(task -> {
            grouped.computeIfAbsent(
                    keyExtractor.apply(task),
                    ignored -> new ArrayList<>()
            ).add(task);
        });

        List<TaskViewResult.Group> groups = grouped.entrySet().stream()
                .map(entry -> new TaskViewResult.Group(
                        entry.getKey().id(),
                        entry.getKey().name(),
                        entry.getKey().position(),
                        List.copyOf(entry.getValue())
                ))
                .toList();

        return new TaskViewResult(groupBy, groups);
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
