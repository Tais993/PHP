package nl.tijsbeek.repositories;

import nl.tijsbeek.entities.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {

    Optional<Task> findById(long id);

    List<Task> findAll();

    Task save(Task task);

    void deleteById(long id);
}