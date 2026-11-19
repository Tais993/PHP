package nl.tijsbeek.repositories.jpa;

import nl.tijsbeek.entities.BoardJPA;
import nl.tijsbeek.entities.BoardStatusJPA;
import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.entities.FilterRule;
import nl.tijsbeek.entities.FilterRule.FilterOperator;
import nl.tijsbeek.entities.TaskJPA;
import org.jspecify.annotations.NonNull;
import org.springframework.lang.Contract;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TaskViewQueriesImpl implements TaskViewQueries {

    private static final EscapeCharacter LIKE_ESCAPE = EscapeCharacter.of('!');

    private final JdbcTemplate jdbcTemplate;

    public TaskViewQueriesImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<TaskJPA> findAllForView(BoardView view) {
        Objects.requireNonNull(view, "View is required");
        Objects.requireNonNull(view.getBoardId(), "View board ID is required");

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

        List<Object> parameters = new ArrayList<>();
        parameters.add(view.getBoardId());
        StringBuilder query = new StringBuilder("""
                SELECT task.id, task.board_id, task.title, task.description, task.created_at, task.due_date,
                       status.id AS status_id, status.board_id AS status_board_id,
                       status.name AS status_name, status.position AS status_position
                FROM task
                LEFT JOIN board_status status ON task.status = status.id
                WHERE task.board_id = ?
                """);

        for (FilterRule rule : view.getFilterRules()) {
            query.append("AND ").append(filterCondition(rule, parameters)).append('\n');
        }
        query.append("ORDER BY ").append(grouping).append(sortField).append(' ')
                .append(sortDirection).append(", task.id ASC");

        return jdbcTemplate.query(query.toString(), this::mapTask, parameters.toArray());
    }

    private @NonNull String filterCondition(@NonNull FilterRule rule, @NonNull List<Object> parameters) {
        return switch (rule.field()) {
            case STATUS -> statusCondition(rule, parameters);
            case TITLE -> titleCondition(rule, parameters);
            case DUE_DATE -> dueDateCondition(rule, parameters);
        };
    }

    private @NonNull String statusCondition(@NonNull FilterRule rule, List<Object> parameters) {
        String operator = switch (rule.operator()) {
            case IN -> "IN";
            case NOT_IN -> "NOT IN";
            default -> throw unsupportedOperator(rule);
        };

        for (String value : rule.values()) {
            parameters.add(Long.valueOf(value));
        }

        String placeholders = String.join(", ", Collections.nCopies(rule.values().size(), "?"));
        return "task.status " + operator + " (" + placeholders + ")";
    }

    private @NonNull String titleCondition(@NonNull FilterRule rule, List<Object> parameters) {
        String title = rule.values().getFirst();

        return switch (rule.operator()) {
            case EQUALS -> {
                parameters.add(title);
                yield "task.title = ?";
            }
            case CONTAINS -> {
                parameters.add("%" + LIKE_ESCAPE.escape(title) + "%");
                yield "LOWER(task.title) LIKE LOWER(?) ESCAPE '" + LIKE_ESCAPE.getEscapeCharacter() + "'";
            }
            default -> throw unsupportedOperator(rule);
        };
    }

    private @NonNull String dueDateCondition(@NonNull FilterRule rule, List<Object> parameters) {
        return switch (rule.operator()) {
            case IS_NULL -> "task.due_date IS NULL";
            case IS_NOT_NULL -> "task.due_date IS NOT NULL";
            case LESS_THAN_OR_EQUAL, GREATER_THAN_OR_EQUAL -> {
                parameters.add(LocalDateTime.parse(rule.values().getFirst()));
                yield rule.operator() == FilterOperator.LESS_THAN_OR_EQUAL
                        ? "task.due_date <= ?"
                        : "task.due_date >= ?";
            }
            default -> throw unsupportedOperator(rule);
        };
    }

    private @NonNull IllegalArgumentException unsupportedOperator(@NonNull FilterRule rule) {
        return new IllegalArgumentException(
                "Unsupported operator " + rule.operator() + " for " + rule.field()
        );
    }

    @Contract("_, _ -> new")
    private @NonNull TaskJPA mapTask(@NonNull ResultSet resultSet, int rowNumber) throws SQLException {
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