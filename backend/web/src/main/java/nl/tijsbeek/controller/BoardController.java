package nl.tijsbeek.controller;

import nl.tijsbeek.dto.request.CreateBoardRequest;
import nl.tijsbeek.dto.response.BoardResponse;
import nl.tijsbeek.dto.response.BoardViewResponse;
import nl.tijsbeek.dto.response.TaskResponse;
import nl.tijsbeek.exceptions.BoardNotFoundException;
import nl.tijsbeek.interfaces.services.BoardService;
import nl.tijsbeek.interfaces.services.BoardViewService;
import nl.tijsbeek.interfaces.services.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/boards")
public class BoardController {
    private final BoardService boardService;
    private final TaskService taskService;
    private final BoardViewService boardViewService;

    public BoardController(BoardService boardService, TaskService taskService, BoardViewService boardViewService) {
        this.boardService = requireNonNull(boardService);
        this.taskService = taskService;
        this.boardViewService = boardViewService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoardResponse create(@RequestBody CreateBoardRequest request) {
        return BoardResponse.fromDomain(boardService.createBoard(request.toDomain()));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<BoardResponse> receive() {
        return boardService.getAllBoards()
                .stream()
                .map(BoardResponse::fromDomain)
                .toList();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BoardResponse receive(@PathVariable final long id) {
        return boardService.getBoard(id)
                .map(BoardResponse::fromDomain)
                .orElseThrow(() -> new BoardNotFoundException(id));
    }

    @GetMapping("/{boardId}/tasks")
    @ResponseStatus(HttpStatus.OK)
    public List<TaskResponse> receiveTasks(@PathVariable final long boardId) {
        return taskService.getAllTasksByBoardId(boardId)
                .stream()
                .map(TaskResponse::fromDomain)
                .toList();
    }

    @GetMapping("/{boardId}/views")
    @ResponseStatus(HttpStatus.OK)
    public List<BoardViewResponse> receiveViews(@PathVariable final long boardId) {
        return boardViewService.getBoardViews(boardId)
                .stream()
                .map(BoardViewResponse::fromDomain)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final long id) {
        boardService.deleteBoard(id);
    }
}
