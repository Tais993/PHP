package nl.tijsbeek.controller;

import nl.tijsbeek.dto.request.CreateTaskRequest;
import nl.tijsbeek.dto.request.UpdateTaskStatusRequest;
import nl.tijsbeek.dto.response.TaskResponse;
import nl.tijsbeek.exceptions.TaskNotFoundException;
import nl.tijsbeek.interfaces.services.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = requireNonNull(taskService);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@RequestBody CreateTaskRequest request) {
        return TaskResponse.fromDomain(taskService.createTask(request.toDomain()));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<TaskResponse> receive() {
        return taskService.getAllTasks()
                .stream()
                .map(TaskResponse::fromDomain)
                .toList();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskResponse receive(@PathVariable long id) {
        return taskService.getTask(id)
                .map(TaskResponse::fromDomain)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @PostMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public TaskResponse updateStatus(@PathVariable long id, @RequestBody UpdateTaskStatusRequest request) {
        return TaskResponse.fromDomain(taskService.changeTaskStatus(request.toDomain(id)));
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        taskService.deleteTask(id);
    }
}
