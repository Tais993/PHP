package nl.tijsbeek.integration.fixtures;

import nl.tijsbeek.testing.fixtures.DatabaseFixture;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class IntegrationTestBase extends DatabaseFixture {

    protected IntegrationTestBase(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }
}