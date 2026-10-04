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

val unitTestReport = tasks.register<JacocoReport>("jacocoUnitTestReport") {
    dependsOn(tasks.named("test"))

    executionData(tasks.named<Test>("test"))

    sourceSets(sourceSets["main"])

    reports {
        xml.required = true
        html.required = true
        csv.required = false

        xml.outputLocation =
            layout.buildDirectory.file(
                "reports/jacoco/unit/jacocoUnitTestReport.xml"
            )

        html.outputLocation =
            layout.buildDirectory.dir(
                "reports/jacoco/unit/html"
            )
    }
}

val integrationTestReport =
    tasks.register<JacocoReport>("jacocoIntegrationTestReport") {

        dependsOn(integrationTest)

        executionData(integrationTest)

        sourceSets(sourceSets["main"])

        reports {
            xml.required = true
            html.required = true
            csv.required = false

            xml.outputLocation =
                layout.buildDirectory.file(
                    "reports/jacoco/integration/jacocoIntegrationTestReport.xml"
                )

            html.outputLocation =
                layout.buildDirectory.dir(
                    "reports/jacoco/integration/html"
                )
        }
    }

tasks.named<Test>("test") {
    finalizedBy(unitTestReport)
}

integrationTest.configure {
    finalizedBy(integrationTestReport)
}