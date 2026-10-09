package nl.tijsbeek.testing;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class DatabaseFixture {

    private final JdbcTemplate jdbcTemplate;

    protected DatabaseFixture(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        var postgres = PostgreSqlTestContainer.getInstance();

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void resetDatabase() {
        jdbcTemplate.execute("""
            TRUNCATE TABLE task
            RESTART IDENTITY CASCADE
            """);
    }
}