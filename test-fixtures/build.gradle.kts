plugins {
    `java-library`
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:4.1.1"))

    api("org.junit.jupiter:junit-jupiter-api")

    api("org.springframework:spring-test")
    api("org.springframework:spring-beans")

    api("org.springframework:spring-jdbc")

    api("org.testcontainers:testcontainers-postgresql")
    api("org.springframework.boot:spring-boot-testcontainers")
}