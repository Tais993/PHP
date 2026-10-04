import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    java
    jacoco
}

val sourceSets = extensions.getByType<SourceSetContainer>()

val integrationTestSourceSet = sourceSets.create("integrationTest") {
    java.srcDir("src/integrationTest/java")
    resources.srcDir("src/integrationTest/resources")

    compileClasspath += sourceSets["main"].output
    runtimeClasspath += sourceSets["main"].output
}

configurations.named("integrationTestImplementation") {
    extendsFrom(configurations["testImplementation"])
}

configurations.named("integrationTestRuntimeOnly") {
    extendsFrom(configurations["testRuntimeOnly"])
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = integrationTestSourceSet.output.classesDirs
    classpath = integrationTestSourceSet.runtimeClasspath

    useJUnitPlatform()

    shouldRunAfter(tasks.named("test"))
}

tasks.named("check") {
    dependsOn(integrationTest)
}

val jacocoTestReport = tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(
        tasks.named("test"),
        integrationTest
    )

    executionData(
        tasks.named<Test>("test"),
        integrationTest
    )

    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
}

tasks.named("check") {
    dependsOn(integrationTest)
    dependsOn(jacocoTestReport)
}
