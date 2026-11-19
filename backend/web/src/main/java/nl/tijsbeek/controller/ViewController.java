package nl.tijsbeek.controller;

import nl.tijsbeek.dto.response.BoardViewResponse;
import nl.tijsbeek.dto.request.ReplaceFilterRulesRequest;
import nl.tijsbeek.dto.response.TaskViewResponse;
import nl.tijsbeek.exceptions.BoardViewNotFoundException;
import nl.tijsbeek.interfaces.services.BoardViewService;
import nl.tijsbeek.interfaces.services.TaskService;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/views")
public class ViewController {

    private final TaskService taskService;
    private final BoardViewService boardViewService;

    public ViewController(TaskService taskService, BoardViewService boardViewService) {
        this.boardViewService = boardViewService;
        this.taskService = taskService;
    }

    @GetMapping("/{viewId}")
    @ResponseStatus(HttpStatus.OK)
    public @NonNull BoardViewResponse receive(@PathVariable final long viewId) {
        return boardViewService.getBoardView(viewId)
                .map(BoardViewResponse::fromDomain)
                .orElseThrow(() -> new BoardViewNotFoundException(viewId));
    }

    @PutMapping("/{viewId}/filter-rules")
    public @NonNull BoardViewResponse replaceFilterRules(
            @PathVariable final long viewId,
            @RequestBody ReplaceFilterRulesRequest request
    ) {
        return BoardViewResponse.fromDomain(boardViewService.replaceFilterRules(viewId, request.toDomain()));
    }

    @GetMapping("/{viewId}/tasks")
    @ResponseStatus(HttpStatus.OK)
    public @NonNull TaskViewResponse receiveTasksOfView(@PathVariable final long viewId) {
        return TaskViewResponse.fromDomain(taskService.getTasksOfBoardView(viewId));
    }
}
