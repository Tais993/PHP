package nl.tijsbeek.controller;

import nl.tijsbeek.dto.request.CreateStatusRequest;
import nl.tijsbeek.dto.request.UpdateBoardStatusRequest;
import nl.tijsbeek.dto.response.StatusResponse;
import nl.tijsbeek.exceptions.BoardStatusNotFoundException;
import nl.tijsbeek.interfaces.services.BoardStatusService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/board-status")
public class BoardStatusController {

    private final BoardStatusService boardStatusService;

    public BoardStatusController(BoardStatusService boardStatusService) {
        this.boardStatusService = boardStatusService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StatusResponse create(@RequestBody final CreateStatusRequest request) {
        return StatusResponse.fromDomain(boardStatusService.createBoardStatus(request.toDomain()));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public StatusResponse receive(@PathVariable long id) {
        return boardStatusService.getBoardStatus(id)
                .map(StatusResponse::fromDomain)
                .orElseThrow(() -> new BoardStatusNotFoundException(id));
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public StatusResponse update(@PathVariable long id, @RequestBody UpdateBoardStatusRequest request) {
        return StatusResponse.fromDomain(boardStatusService.updateBoardStatus(id, request.titleToFieldUpdate(), request.positionToFieldUpdate()));
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        boardStatusService.deleteBoardStatus(id);
    }
}
