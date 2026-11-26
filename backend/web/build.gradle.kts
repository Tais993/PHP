plugins {
    id("org.springframework.boot")
    id("org.asciidoctor.jvm.convert")
}

description = "Web"

extra["snippetsDir"] = file("build/generated-snippets")

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))
    implementation(project(":infrastructure"))

    implementation("org.jspecify:jspecify")
    implementation("org.openapitools:jackson-databind-nullable:0.2.13")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    annotationProcessor(
        "org.springframework.boot:spring-boot-configuration-processor"
    )


    testImplementation(project(":test-fixtures"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-restdocs")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

    testImplementation("org.springframework.boot:spring-boot-starter-jackson")

    add(mockitoAgent.name, "org.mockito:mockito-core")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    outputs.dir(project.extra["snippetsDir"]!!)
}

tasks.asciidoctor {
    inputs.dir(project.extra["snippetsDir"]!!)
    dependsOn(tasks.test)
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    systemProperty("spring.profiles.active", "local")
}

tasks.register<Sync>("prepareE2eBackend") {
    group = "verification"
    description = "Stages the application and JaCoCo agent for end-to-end coverage."
    into(layout.buildDirectory.dir("e2e"))

    from(tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar")) {
        rename { "application.jar" }
    }
    from({
        configurations["jacocoAgent"].map { zipTree(it) }
    }) {
        include("jacocoagent.jar")
    }
}
