plugins {
    id("java")
}

description = "Application"

dependencies {
    implementation(project(":domain"))

    implementation("org.jspecify:jspecify")
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-core")
    implementation("org.springframework:spring-tx")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-junit-jupiter")
}
