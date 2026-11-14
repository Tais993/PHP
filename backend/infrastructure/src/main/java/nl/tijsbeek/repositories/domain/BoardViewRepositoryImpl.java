package nl.tijsbeek.repositories.domain;

import jakarta.persistence.EntityManager;
import nl.tijsbeek.entities.BoardJPA;
import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.entities.BoardViewJPA;
import nl.tijsbeek.interfaces.repositories.BoardViewRepository;
import nl.tijsbeek.mapper.BoardViewMapper;
import nl.tijsbeek.repositories.jpa.BoardViewJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class BoardViewRepositoryImpl implements BoardViewRepository {

    private final BoardViewJpaRepository jpaRepository;
    private final EntityManager entityManager;

    public BoardViewRepositoryImpl(BoardViewJpaRepository jpaRepository, EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BoardView> findById(long id) {
        return jpaRepository.findById(id).map(BoardViewMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardView> findAll() {
        return jpaRepository.findAll().stream().map(BoardViewMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardView> findAllByBoardId(long boardId) {
        return jpaRepository.findAllByBoardIdOrderByPositionAscIdAsc(boardId)
                .stream()
                .map(BoardViewMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public BoardView save(BoardView view) {
        BoardJPA board = entityManager.getReference(BoardJPA.class, view.getBoardId());
        BoardViewJPA entity = BoardViewMapper.toJpa(view, board);
        return BoardViewMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public void deleteById(long id) {
        jpaRepository.deleteById(id);
    }
}
