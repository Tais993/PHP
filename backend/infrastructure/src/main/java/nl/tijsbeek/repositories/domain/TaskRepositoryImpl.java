package nl.tijsbeek.repositories.domain;

import nl.tijsbeek.entities.Task;
import nl.tijsbeek.entities.TaskJPA;
import nl.tijsbeek.mapper.TaskMapper;
import nl.tijsbeek.repositories.TaskRepository;
import nl.tijsbeek.repositories.jpa.TaskJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TaskRepositoryImpl implements TaskRepository {

    private final TaskJpaRepository jpaRepository;

    public TaskRepositoryImpl(TaskJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Task> findById(long id) {
        return jpaRepository.findById(id)
                .map(TaskMapper::toDomain);
    }

    @Override
    public List<Task> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(TaskMapper::toDomain)
                .toList();
    }

    @Override
    public Task save(Task task) {
        TaskJPA entity = TaskMapper.toJpa(task);

        TaskJPA saved = jpaRepository.save(entity);

        return TaskMapper.toDomain(saved);
    }

    @Override
    public void deleteById(long id) {
        jpaRepository.deleteById(id);
    }
}