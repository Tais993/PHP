package nl.tijsbeek.integration.fixtures;

import nl.tijsbeek.testing.fixtures.DatabaseFixture;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class IntegrationTestBase extends DatabaseFixture {

    private final JdbcTemplate jdbcTemplate;

    protected IntegrationTestBase(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.jdbcTemplate = jdbcTemplate;
    }

    protected long createBoard(String title, String description) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO board (title, description) VALUES (?, ?) RETURNING id",
                Long.class, title, description);
    }

    protected long createStatus(long boardId, String name, int position) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO board_status (board_id, name, position) VALUES (?, ?, ?) RETURNING id",
                Long.class, boardId, name, position);
    }

    protected long createTask(long boardId, String title, String description, Long statusId) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO task (board_id, title, description, status) VALUES (?, ?, ?, ?) RETURNING id",
                Long.class, boardId, title, description, statusId);
    }

    protected long createView(long boardId, String name, String type, String groupBy,
                              String sortBy, String sortDirection, int position) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO board_view (board_id, name, type, group_by, sort_by, sort_direction, position)
                VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id
                """, Long.class, boardId, name, type, groupBy, sortBy, sortDirection, position);
    }
}
