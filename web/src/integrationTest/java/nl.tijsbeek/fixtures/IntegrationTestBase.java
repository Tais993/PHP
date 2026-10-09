package nl.tijsbeek.fixtures;

import nl.tijsbeek.testing.DatabaseFixture;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestBase extends DatabaseFixture {

    protected IntegrationTestBase(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }
}