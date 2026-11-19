plugins {
}

description = "Infrastructure"

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))

    implementation("org.jspecify:jspecify")
    implementation("org.springframework:spring-core")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-restclient")

    implementation("org.springframework.boot:spring-boot-starter-flyway")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation(project(":test-fixtures"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    add(mockitoAgent.name, "org.mockito:mockito-core")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
