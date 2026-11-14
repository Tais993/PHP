package nl.tijsbeek.repositories.domain;

import jakarta.persistence.EntityManager;
import nl.tijsbeek.entities.BoardStatusJPA;
import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.entities.Task;
import nl.tijsbeek.entities.TaskJPA;
import nl.tijsbeek.interfaces.repositories.BoardViewRepository;
import nl.tijsbeek.interfaces.repositories.TaskRepository;
import nl.tijsbeek.mapper.TaskMapper;
import nl.tijsbeek.repositories.jpa.TaskJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TaskRepositoryImpl implements TaskRepository {

    private final BoardViewRepository boardViewRepository;
    private final TaskJpaRepository jpaRepository;
    private final EntityManager entityManager;

    public TaskRepositoryImpl(BoardViewRepository boardViewRepository, TaskJpaRepository jpaRepository, EntityManager entityManager) {
        this.boardViewRepository = boardViewRepository;
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Task> findById(long id) {
        return jpaRepository.findById(id)
                .map(TaskMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(TaskMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findAllByBoardId(long boardId) {
        return jpaRepository.findAllByBoardId(boardId)
                .stream()
                .map(TaskMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findAllForView(long boardViewId) {
        return boardViewRepository.findById(boardViewId)
                .map(this::findAllForView)
                .orElseGet(ArrayList::new);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findAllForView(BoardView view) {
        return jpaRepository.findAllForView(view)
                .stream()
                .map(TaskMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public Task save(Task task) {
        BoardStatusJPA status = task.getStatus() == null ? null :
                entityManager.getReference(BoardStatusJPA.class, task.getStatus().getId());
        TaskJPA entity = TaskMapper.toJpa(task, status);

        TaskJPA saved = jpaRepository.save(entity);

        return TaskMapper.toDomain(saved);
    }

    @Override
    public void deleteById(long id) {
        jpaRepository.deleteById(id);
    }
}
