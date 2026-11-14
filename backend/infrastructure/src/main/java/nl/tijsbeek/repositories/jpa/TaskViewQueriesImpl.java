package nl.tijsbeek.repositories.jpa;

import jakarta.persistence.EntityManager;
import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.entities.TaskJPA;

import java.util.List;
import java.util.Objects;

public class TaskViewQueriesImpl implements TaskViewQueries {

    private final EntityManager entityManager;

    public TaskViewQueriesImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<TaskJPA> findAllForView(BoardView view) {
        Objects.requireNonNull(view, "View is required");
        Objects.requireNonNull(view.getBoardId(), "View board ID is required");

        // Only enum-selected expressions enter the query; board ID remains a bound parameter.
        String grouping = switch (view.getGroupBy()) {
            case null -> "";
            case STATUS -> "status.position ASC NULLS LAST, status.id ASC NULLS LAST, ";
        };
        String sortField = switch (view.getSortBy()) {
            case TITLE -> "task.title";
            case CREATED_AT -> "task.createdAt";
        };
        String sortDirection = switch (view.getSortDirection()) {
            case ASC -> "ASC";
            case DESC -> "DESC";
        };

        String query = """
                SELECT task FROM TaskJPA task
                LEFT JOIN FETCH task.status status
                WHERE task.boardId = :boardId
                ORDER BY
                """ + grouping + sortField + " " + sortDirection + ", task.id ASC";

        return entityManager.createQuery(query, TaskJPA.class)
                .setParameter("boardId", view.getBoardId())
                .getResultList();
    }
}
