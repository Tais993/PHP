package nl.tijsbeek.repositories.domain;

import nl.tijsbeek.entities.Board;
import nl.tijsbeek.entities.BoardJPA;
import nl.tijsbeek.mapper.BoardMapper;
import nl.tijsbeek.interfaces.repositories.BoardRepository;
import nl.tijsbeek.repositories.jpa.BoardJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BoardRepositoryImpl implements BoardRepository {

private final BoardJpaRepository jpaRepository;

    public BoardRepositoryImpl(BoardJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Board> findById(long id) {
        return jpaRepository.findById(id)
                .map(BoardMapper::toDomain);
    }

    @Override
    public List<Board> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(BoardMapper::toDomain)
                .toList();
    }

    @Override
    public Board save(Board board) {
        BoardJPA entity = BoardMapper.toJpa(board);

        BoardJPA saved = jpaRepository.save(entity);

        return BoardMapper.toDomain(saved);
    }

    @Override
    public void deleteById(long id) {
        jpaRepository.deleteById(id);
    }
}