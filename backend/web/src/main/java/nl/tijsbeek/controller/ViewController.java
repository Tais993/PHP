package nl.tijsbeek.controller;

import nl.tijsbeek.dto.request.CreateBoardViewRequest;
import nl.tijsbeek.dto.request.ReplaceFilterRulesRequest;
import nl.tijsbeek.dto.request.UpdateBoardViewDetails;
import nl.tijsbeek.dto.request.UpdateSortingView;
import nl.tijsbeek.dto.response.BoardViewResponse;
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

    @PostMapping
    public @NonNull BoardViewResponse create(@RequestBody CreateBoardViewRequest request) {
        return BoardViewResponse.fromDomain(boardViewService.createBoardView(request.toDomain()));
    }

    @PostMapping("/{viewId}/details")
    public @NonNull BoardViewResponse update(@PathVariable final long viewId, @RequestBody UpdateBoardViewDetails details) {
        return BoardViewResponse.fromDomain(boardViewService.updateBoardViewDetails(viewId, details.name(), details.position()));
    }

    @PostMapping("/{viewId}/duplicate")
    public @NonNull BoardViewResponse duplicate(@PathVariable final long viewId, @RequestBody String name) {
        return BoardViewResponse.fromDomain(boardViewService.duplicateBoardView(viewId, name));
    }

    @PutMapping("/{viewId}/filter-rules")
    public @NonNull BoardViewResponse replaceFilterRules(
            @PathVariable final long viewId,
            @RequestBody ReplaceFilterRulesRequest request
    ) {
        return BoardViewResponse.fromDomain(boardViewService.replaceFilterRules(viewId, request.toDomain()));
    }

    @PutMapping("/{viewId}/sorting")
    public @NonNull BoardViewResponse updateSortingView(
            @PathVariable final long viewId,
            @RequestBody UpdateSortingView request
    ) {
        return BoardViewResponse.fromDomain(boardViewService.updateSortingOfBoardView(viewId, request.sortBy(), request.sortDirection()));
    }

    @GetMapping("/{viewId}/tasks")
    @ResponseStatus(HttpStatus.OK)
    public @NonNull TaskViewResponse receiveTasksOfView(@PathVariable final long viewId) {
        return TaskViewResponse.fromDomain(taskService.getTasksOfBoardView(viewId));
    }
}
