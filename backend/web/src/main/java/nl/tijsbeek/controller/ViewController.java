package nl.tijsbeek.controller;

import nl.tijsbeek.dto.BoardViewResponse;
import nl.tijsbeek.dto.TaskViewResponse;
import nl.tijsbeek.interfaces.services.BoardViewService;
import nl.tijsbeek.interfaces.services.TaskService;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/views")
public class ViewController {

    private final TaskService taskService;
    private final BoardViewService boardViewService;

    @Contract(pure = true)
    public ViewController(TaskService taskService, BoardViewService boardViewService) {
        this.boardViewService = boardViewService;
        this.taskService = taskService;
    }

    @GetMapping("/{viewId}")
    @ResponseStatus(HttpStatus.OK)
    public @NotNull ResponseEntity<BoardViewResponse> receive(@PathVariable final long viewId) {
        return  boardViewService.getBoardView(viewId)
                .map(BoardViewResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{viewId}/tasks")
    @ResponseStatus(HttpStatus.OK)
    public @NotNull TaskViewResponse receiveTasksOfView(@PathVariable final long viewId) {
        return TaskViewResponse.fromDomain(taskService.getTasksOfBoardView(viewId));
    }
}
