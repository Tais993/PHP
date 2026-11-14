package nl.tijsbeek.repositories.jpa;

import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.entities.TaskJPA;

import java.util.List;

public interface TaskViewQueries {

    List<TaskJPA> findAllForView(BoardView view);
}
