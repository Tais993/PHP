package nl.tijsbeek.testing;

import org.jetbrains.annotations.Contract;
import org.testcontainers.postgresql.PostgreSQLContainer;

public final class PostgreSqlTestContainer {

    private static final PostgreSQLContainer INSTANCE =
            new PostgreSQLContainer("postgres:17");

    static {
        INSTANCE.start();
    }

    @Contract(pure = true)
    private PostgreSqlTestContainer() {
    }

    @Contract(pure = true)
    public static PostgreSQLContainer getInstance() {
        return INSTANCE;
    }
}