package nl.tijsbeek.testing;

import org.testcontainers.postgresql.PostgreSQLContainer;

public final class PostgreSqlTestContainer {

    private static final PostgreSQLContainer INSTANCE =
            new PostgreSQLContainer("postgres:17");

    static {
        INSTANCE.start();
    }

    private PostgreSqlTestContainer() {
    }

    public static PostgreSQLContainer getInstance() {
        return INSTANCE;
    }
}