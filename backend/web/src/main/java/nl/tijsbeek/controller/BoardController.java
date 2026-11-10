package nl.tijsbeek.controller;

import nl.tijsbeek.dto.BoardResponse;
import nl.tijsbeek.dto.CreateBoardRequest;
import nl.tijsbeek.dto.TaskResponse;
import nl.tijsbeek.entities.Board;
import nl.tijsbeek.interfaces.services.BoardService;
import nl.tijsbeek.interfaces.services.TaskService;
import org.jetbrains.annotations.Contract;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/boards")
public class BoardController {

    private final BoardService boardService;

    @Contract(pure = true)
    public BoardController(BoardService boardService) {
        this.boardService = requireNonNull(boardService);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoardResponse create(@RequestBody CreateBoardRequest request) {
        Board board = boardService.createBoard(request.title(), request.description());

        return BoardResponse.fromDomain(board);
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
    public ResponseEntity<BoardResponse> receive(@PathVariable final long id) {
        return  boardService.getBoard(id)
                .map(BoardResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final long id) {
        boardService.deleteBoard(id);
    }
}
