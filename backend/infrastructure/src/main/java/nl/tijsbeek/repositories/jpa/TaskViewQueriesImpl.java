package nl.tijsbeek.repositories.jpa;

import nl.tijsbeek.entities.BoardJPA;
import nl.tijsbeek.entities.BoardStatusJPA;
import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.entities.TaskJPA;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class TaskViewQueriesImpl implements TaskViewQueries {

    private final JdbcTemplate jdbcTemplate;

    public TaskViewQueriesImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<TaskJPA> findAllForView(BoardView view) {
        Objects.requireNonNull(view, "View is required");
        Objects.requireNonNull(view.getBoardId(), "View board ID is required");

        // SQL identifiers cannot be bound parameters; select them only from enum-controlled expressions.
        String grouping = switch (view.getGroupBy()) {
            case null -> "";
            case STATUS -> "status.position ASC NULLS LAST, status.id ASC NULLS LAST, ";
        };
        String sortField = switch (view.getSortBy()) {
            case TITLE -> "task.title";
            case CREATED_AT -> "task.created_at";
        };
        String sortDirection = switch (view.getSortDirection()) {
            case ASC -> "ASC";
            case DESC -> "DESC";
        };

        String query = """
                SELECT task.id, task.board_id, task.title, task.description, task.created_at, task.due_date,
                       status.id AS status_id, status.board_id AS status_board_id,
                       status.name AS status_name, status.position AS status_position
                FROM task
                LEFT JOIN board_status status ON task.status = status.id
                WHERE task.board_id = ?
                ORDER BY
                """ + grouping + sortField + " " + sortDirection + ", task.id ASC";

        return jdbcTemplate.query(connection -> {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, view.getBoardId());
            return statement;
        }, this::mapTask);
    }

    private TaskJPA mapTask(ResultSet resultSet, int rowNumber) throws SQLException {
        Long statusId = resultSet.getObject("status_id", Long.class);
        BoardStatusJPA status = null;

        if (statusId != null) {
            BoardJPA board = new BoardJPA();
            board.setId(resultSet.getLong("status_board_id"));
            status = new BoardStatusJPA(
                    statusId,
                    board,
                    resultSet.getString("status_name"),
                    resultSet.getInt("status_position")
            );
        }

        return new TaskJPA(
                resultSet.getLong("id"),
                resultSet.getLong("board_id"),
                resultSet.getString("title"),
                resultSet.getString("description"),
                resultSet.getObject("created_at", LocalDateTime.class),
                resultSet.getObject("due_date", LocalDateTime.class),
                status
        );
    }
}
