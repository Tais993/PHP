package nl.tijsbeek.controller;

import nl.tijsbeek.dto.CreateTaskRequest;
import nl.tijsbeek.dto.TaskResponse;
import nl.tijsbeek.entities.Task;
import nl.tijsbeek.services.TaskService;
import org.jetbrains.annotations.Contract;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;

    @Contract(pure = true)
    public TaskController(TaskService taskService) {
        this.taskService = requireNonNull(taskService);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@RequestBody CreateTaskRequest taskRequest) {
        Task task = taskService.createTask(taskRequest.title(), taskRequest.description(), taskRequest.status());
        return TaskResponse.fromDomain(task);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TaskResponse> receive(@RequestParam long id) {
        return taskService.getTask(id)
                .map(TaskResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
