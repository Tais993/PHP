import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    java
    jacoco
}

val sourceSets = extensions.getByType<SourceSetContainer>()

val integrationTestSourceSet = sourceSets.create("integrationTest")

configurations.named("integrationTestImplementation") {
    extendsFrom(configurations["testImplementation"])
}

configurations.named("integrationTestRuntimeOnly") {
    extendsFrom(configurations["testRuntimeOnly"])
}

integrationTestSourceSet.compileClasspath =
    sourceSets["main"].output +
        configurations["testCompileClasspath"]

integrationTestSourceSet.runtimeClasspath =
    integrationTestSourceSet.output +
        sourceSets["main"].output +
        configurations["testRuntimeClasspath"]

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    testLogging {
        showStandardStreams = true
    }
}

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = integrationTestSourceSet.output.classesDirs
    classpath = integrationTestSourceSet.runtimeClasspath

    shouldRunAfter(tasks.named("test"))
}

tasks.named("check") {
    dependsOn(integrationTest)
}

val unitTestReport = tasks.register<JacocoReport>("jacocoUnitTestReport") {
    dependsOn(tasks.named("test"))

    executionData(
        layout.buildDirectory.file("jacoco/test.exec")
    )

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

        executionData(
            layout.buildDirectory.file("jacoco/integrationTest.exec")
        )

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