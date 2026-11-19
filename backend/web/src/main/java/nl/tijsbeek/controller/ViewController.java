package nl.tijsbeek.controller;

import nl.tijsbeek.dto.BoardViewResponse;
import nl.tijsbeek.dto.ReplaceFilterRulesRequest;
import nl.tijsbeek.dto.TaskViewResponse;
import nl.tijsbeek.exceptions.BoardViewNotFoundException;
import nl.tijsbeek.interfaces.services.BoardViewService;
import nl.tijsbeek.interfaces.services.TaskService;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    public @NotNull BoardViewResponse receive(@PathVariable final long viewId) {
        return boardViewService.getBoardView(viewId)
                .map(BoardViewResponse::fromDomain)
                .orElseThrow(() -> new BoardViewNotFoundException(viewId));
    }

    @PutMapping("/{viewId}/filter-rules")
    public @NotNull BoardViewResponse replaceFilterRules(
            @PathVariable final long viewId,
            @RequestBody ReplaceFilterRulesRequest request
    ) {
        return BoardViewResponse.fromDomain(boardViewService.replaceFilterRules(viewId, request.toDomain()));
    }

    @GetMapping("/{viewId}/tasks")
    @ResponseStatus(HttpStatus.OK)
    public @NotNull TaskViewResponse receiveTasksOfView(@PathVariable final long viewId) {
        return TaskViewResponse.fromDomain(taskService.getTasksOfBoardView(viewId));
    }
}
