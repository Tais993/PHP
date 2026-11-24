package nl.tijsbeek.integration.migration;

import nl.tijsbeek.testing.annotations.IntegrationTest;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class TaskStatusV4_1MigrationIntegrationTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    private PostgreSQLContainer postgres;
    private JdbcTemplate jdbcTemplate;
    private Flyway flyway;
    private long boardId;
    private long statusId;
    private long assignedTaskId;
    private long unassignedTaskId;

    @BeforeEach
    void setUp() {
        postgres = new PostgreSQLContainer("postgres:17");
        postgres.start();
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()
        );
        jdbcTemplate = new JdbcTemplate(dataSource);

        Flyway.configure().dataSource(dataSource).target("4").load().migrate();

        boardId = jdbcTemplate.queryForObject(
                "INSERT INTO board (title, description) VALUES (?, ?) RETURNING id",
                Long.class, "BoardOne", "Board for migration testing"
        );
        statusId = jdbcTemplate.queryForObject(
                "INSERT INTO board_status (board_id, name, position) VALUES (?, ?, ?) RETURNING id",
                Long.class, boardId, "To do", 0
        );
        assignedTaskId = createTask("Assigned task", "Task with a status", statusId);
        unassignedTaskId = createTask("Unassigned task", "Task without a status", null);

        flyway = Flyway.configure().dataSource(dataSource).target("4.1").load();
    }

    @AfterEach
    void tearDown() {
        if (postgres != null) {
            postgres.stop();
        }
    }

    @Test
    void migrate_PreservesAssignedAndUnassignedTasks() {
        MigrateResult result = flyway.migrate();

        assertThat(result.migrationsExecuted).isEqualTo(1);

        assertThat(jdbcTemplate.queryForMap("SELECT * FROM task WHERE id = ?", assignedTaskId))
                .containsEntry("id", assignedTaskId)
                .containsEntry("board_id", boardId)
                .containsEntry("title", "Assigned task")
                .containsEntry("description", "Task with a status")
                .containsEntry("created_at", Timestamp.valueOf(CREATED_AT))
                .containsEntry("status", statusId);

        assertThat(jdbcTemplate.queryForMap("SELECT * FROM task WHERE id = ?", unassignedTaskId))
                .containsEntry("id", unassignedTaskId)
                .containsEntry("board_id", boardId)
                .containsEntry("title", "Unassigned task")
                .containsEntry("description", "Task without a status")
                .containsEntry("created_at", Timestamp.valueOf(CREATED_AT))
                .containsEntry("status", null);

        assertThat(jdbcTemplate.queryForMap("SELECT * FROM board WHERE id = ?", boardId))
                .containsEntry("title", "BoardOne")
                .containsEntry("description", "Board for migration testing");

        assertThat(jdbcTemplate.queryForMap("SELECT * FROM board_status WHERE id = ?", statusId))
                .containsEntry("board_id", boardId)
                .containsEntry("name", "To do")
                .containsEntry("position", 0);
    }

    private long createTask(String title, String description, Long taskStatusId) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO task (board_id, title, description, status, created_at) "
                        + "VALUES (?, ?, ?, ?, ?) RETURNING id",
                Long.class, boardId, title, description, taskStatusId, Timestamp.valueOf(CREATED_AT)
        );
    }
}
