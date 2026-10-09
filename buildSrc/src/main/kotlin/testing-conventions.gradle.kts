import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    java
    jacoco
}

val testSourceSet = sourceSets.test.get()

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform {
        excludeTags("integration")
    }
}

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = testSourceSet.output.classesDirs
    classpath = testSourceSet.runtimeClasspath

    useJUnitPlatform {
        includeTags("integration")
    }

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

    sourceSets(sourceSets.main.get())

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

val integrationTestReport = tasks.register<JacocoReport>("jacocoIntegrationTestReport") {
    dependsOn(integrationTest)

    executionData(
        layout.buildDirectory.file("jacoco/integrationTest.exec")
    )

    sourceSets(sourceSets.main.get())

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