import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    jacoco
}

val coverageProjects = rootProject.subprojects.filter {
    it.name != "test-fixtures"
}

fun registerAggregateCoverageReport(
    taskName: String,
    reportDirectory: String,
    configure: JacocoReport.() -> Unit
) {
    tasks.register<JacocoReport>(taskName) {
        group = "verification"
        description = "Aggregates $reportDirectory coverage across all production modules."

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

        configure()
    }
}

fun registerAggregateTestCoverageReport(taskName: String, testTaskName: String, reportDirectory: String) {
    registerAggregateCoverageReport(taskName, reportDirectory) {
        dependsOn(coverageProjects.map { "${it.path}:$testTaskName" })
        executionData.from(coverageProjects.map { it.layout.buildDirectory.file("jacoco/$testTaskName.exec") })
    }
}

registerAggregateTestCoverageReport("jacocoAggregateUnitReport", "test", "unit")
registerAggregateTestCoverageReport("jacocoAggregateIntegrationReport", "integrationTest", "integration")

registerAggregateCoverageReport("jacocoAggregateE2eReport", "e2e") {
    dependsOn(coverageProjects.map { "${it.path}:classes" })
    executionData.from(layout.buildDirectory.file("jacoco/e2e.exec"))
}

tasks.register("testAllWithAggregateCoverage") {
    group = "verification"
    description = "Runs unit and integration tests and generates aggregated coverage reports."

    dependsOn(
        "jacocoAggregateUnitReport",
        "jacocoAggregateIntegrationReport"
    )
}
