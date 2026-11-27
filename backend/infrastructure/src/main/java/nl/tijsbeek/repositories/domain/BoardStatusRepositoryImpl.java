package nl.tijsbeek.repositories.domain;

import jakarta.persistence.EntityManager;
import nl.tijsbeek.entities.BoardJPA;
import nl.tijsbeek.entities.BoardStatus;
import nl.tijsbeek.entities.BoardStatusJPA;
import nl.tijsbeek.interfaces.repositories.BoardStatusRepository;
import nl.tijsbeek.mapper.BoardStatusMapper;
import nl.tijsbeek.repositories.jpa.BoardStatusJpaRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class BoardStatusRepositoryImpl implements BoardStatusRepository {

    private final BoardStatusJpaRepository jpaRepository;
    private final EntityManager entityManager;

    public BoardStatusRepositoryImpl(BoardStatusJpaRepository jpaRepository, EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<BoardStatus> findById(long id) {
        return jpaRepository.findById(id)
                .map(BoardStatusMapper::toDomain);
    }

    @Override
    public Optional<BoardStatus> findByIdAndBoardId(long statusId, long boardId) {
        return jpaRepository.findByIdAndBoardId(statusId, boardId)
                .map(BoardStatusMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardStatus> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(BoardStatusMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardStatus> findAllByBoardId(long boardId) {
        return jpaRepository.findAllByBoardId(boardId)
                .stream()
                .map(BoardStatusMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public BoardStatus save(BoardStatus boardStatus) {
        BoardStatusJPA entity = BoardStatusMapper.toJpa(boardStatus);
        entity.setBoard(boardStatus.getBoardId() == null ? null :
                entityManager.getReference(BoardJPA.class, boardStatus.getBoardId()));

        BoardStatusJPA saved = jpaRepository.save(entity);

        return BoardStatusMapper.toDomain(saved);
    }

    @Override
    @Transactional
    public List<BoardStatus> saveAll(List<BoardStatus> boardStatuses) {
        List<BoardStatusJPA> entities = boardStatuses.stream()
                .map(this::mapToJpaWithBoard)
                .toList();

        List<BoardStatusJPA> savedEntities = jpaRepository.saveAll(entities);

        return savedEntities.stream()
                .map(BoardStatusMapper::toDomain)
                .toList();
    }

    private BoardStatusJPA mapToJpaWithBoard(@NonNull BoardStatus boardStatus) {
        BoardStatusJPA entity = BoardStatusMapper.toJpa(boardStatus);
        entity.setBoard(boardStatus.getBoardId() == null ? null :
                entityManager.getReference(BoardJPA.class, boardStatus.getBoardId()));

        return entity;
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        jpaRepository.deleteById(id);
    }
}
