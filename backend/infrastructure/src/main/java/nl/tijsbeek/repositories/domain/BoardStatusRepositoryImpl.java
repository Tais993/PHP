package nl.tijsbeek.repositories.domain;

import nl.tijsbeek.entities.BoardStatus;
import nl.tijsbeek.entities.BoardStatusJPA;
import nl.tijsbeek.interfaces.repositories.BoardStatusRepository;
import nl.tijsbeek.mapper.BoardStatusMapper;
import nl.tijsbeek.repositories.jpa.BoardStatusJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public class BoardStatusRepositoryImpl implements BoardStatusRepository {

    private final BoardStatusJpaRepository jpaRepository;

    public BoardStatusRepositoryImpl(BoardStatusJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<BoardStatus> findById(long id) {
        return jpaRepository.findById(id)
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

        BoardStatusJPA saved = jpaRepository.save(entity);

        return BoardStatusMapper.toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        jpaRepository.deleteById(id);
    }
}
