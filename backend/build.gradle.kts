import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    java
    jacoco

    id("coverage-aggregation")

    id("org.springframework.boot") version "4.2.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("org.asciidoctor.jvm.convert") version "4.0.5" apply false

    id("org.sonarqube") version "7.6.0.18749"
}

group = "nl.tijsbeek"
version = "0.0.1-SNAPSHOT"
description = "Homepage"

allprojects {
    repositories {
        mavenCentral()
    }

    dependencyLocking {
        lockAllConfigurations()
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version

    apply(plugin = "java")
    apply(plugin = "testing-conventions")
    apply(plugin = "io.spring.dependency-management")

    extensions.configure<DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:4.2.0")
            mavenBom("org.springdoc:springdoc-openapi-bom:3.2.0")
        }
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(27)
        }
    }
}

sonar {
    properties {
        val repositoryDir = rootProject.projectDir.parentFile
        val frontendDir = repositoryDir.resolve("frontend")

        property("sonar.projectKey", "Tais993_PHP")
        property("sonar.organization", "tais993")
        property("sonar.projectBaseDir", repositoryDir.absolutePath)
        property("sonar.sources", frontendDir.resolve("src").absolutePath)
        property("sonar.typescript.tsconfigPaths", frontendDir.resolve("tsconfig.app.json").absolutePath)
        property("sonar.javascript.lcov.reportPaths", frontendDir.resolve("coverage/lcov.info").absolutePath)

        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            listOf(
                layout.buildDirectory.file(
                    "reports/jacoco/unit/report.xml"
                ).get().asFile.absolutePath,

                layout.buildDirectory.file(
                    "reports/jacoco/integration/report.xml"
                ).get().asFile.absolutePath,

                layout.buildDirectory.file(
                    "reports/jacoco/e2e/report.xml"
                ).get().asFile.absolutePath
            ).joinToString(",")
        )

        // Test-fixtures should NOT count for coverage.
        property(
            "sonar.coverage.exclusions",
            "**/test-fixtures/**"
        )

        // JPA and domain entities are NOT duplicates.
        property(
            "sonar.cpd.exclusions",
            "**/*JPA.java,**/entities/**"
        )
    }


    // Runs dependencies --write-locks for each subproject
    tasks.register("updateAllLocks") {
        group = "build setup"
        description = "Updates dependency lockfiles for all projects."

        dependsOn(allprojects.map { "${it.path.trimEnd(':')}:dependencies" })
    }
}
