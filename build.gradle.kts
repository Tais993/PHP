import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    java

    id("org.springframework.boot") version "4.1.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("org.asciidoctor.jvm.convert") version "4.0.5" apply false

    id("org.sonarqube") version "7.5.0.8588"
}

group = "nl.tijsbeek"
version = "0.0.1-SNAPSHOT"
description = "Homepage"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version

    apply(plugin = "java")
    apply(plugin = "testing-conventions")
    apply(plugin = "io.spring.dependency-management")


    dependencies {
        "compileOnly"("org.jetbrains:annotations:26.1.0")
        "testCompileOnly"("org.jetbrains:annotations:26.1.0")
    }


    extensions.configure<DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:4.1.1")
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
        property("sonar.projectKey", "Tais993_PHP")
        property("sonar.organization", "tais993")

        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            listOf(
                "domain/build/reports/jacoco/unit/jacocoUnitTestReport.xml",
                "domain/build/reports/jacoco/integration/jacocoIntegrationTestReport.xml",
                "application/build/reports/jacoco/unit/jacocoUnitTestReport.xml",
                "application/build/reports/jacoco/integration/jacocoIntegrationTestReport.xml",
                "infrastructure/build/reports/jacoco/unit/jacocoUnitTestReport.xml",
                "infrastructure/build/reports/jacoco/integration/jacocoIntegrationTestReport.xml",
                "web/build/reports/jacoco/unit/jacocoUnitTestReport.xml",
                "web/build/reports/jacoco/integration/jacocoIntegrationTestReport.xml"
            ).joinToString(",")
        )
    }
}