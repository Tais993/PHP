package nl.tijsbeek.integration.fixtures;

import nl.tijsbeek.integration.InfrastructureTestConfiguration;
import nl.tijsbeek.testing.fixtures.DatabaseFixture;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(
        classes = InfrastructureTestConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.jpa.hibernate.ddl-auto=validate"
)
public abstract class IntegrationTestBase extends DatabaseFixture {

    protected IntegrationTestBase(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }
}