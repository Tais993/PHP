plugins {
    id("java")
}

description = "Application"

dependencies {
    implementation(project(":domain"))

    implementation("org.springframework:spring-context")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-junit-jupiter")
}