import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    jacoco
}

val coverageProjects = rootProject.subprojects.filter {
    it.name != "test-fixtures"
}

fun registerAggregateCoverageReport(
    taskName: String,
    testTaskName: String,
    reportDirectory: String
) {
    tasks.register<JacocoReport>(taskName) {
        group = "verification"
        description = "Aggregates $testTaskName coverage across all production modules."

        dependsOn(
            coverageProjects.map {
                "${it.path}:$testTaskName"
            }
        )

        classDirectories.from(
            coverageProjects.map {
                it.layout.buildDirectory.dir("classes/java/main")
            }
        )

        sourceDirectories.from(
            coverageProjects.map {
                it.file("src/main/java")
            }
        )

        executionData.from(
            coverageProjects.map {
                it.layout.buildDirectory.file(
                    "jacoco/$testTaskName.exec"
                )
            }
        )

        reports {
            xml.required = true
            html.required = true
            csv.required = false

            xml.outputLocation =
                layout.buildDirectory.file(
                    "reports/jacoco/$reportDirectory/report.xml"
                )

            html.outputLocation =
                layout.buildDirectory.dir(
                    "reports/jacoco/$reportDirectory/html"
                )
        }
    }
}

registerAggregateCoverageReport(
    taskName = "jacocoAggregateUnitReport",
    testTaskName = "test",
    reportDirectory = "unit"
)

registerAggregateCoverageReport(
    taskName = "jacocoAggregateIntegrationReport",
    testTaskName = "integrationTest",
    reportDirectory = "integration"
)

tasks.register("testAllWithAggregateCoverage") {
    group = "verification"
    description = "Runs unit and integration tests and generates aggregated coverage reports."

    dependsOn(
        "jacocoAggregateUnitReport",
        "jacocoAggregateIntegrationReport"
    )
}