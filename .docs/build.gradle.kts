repositories {
    mavenCentral()
}

val plantuml = configurations.create("plantuml")

dependencies {
    // The standalone CLI jar contains the Smetana renderer used by this task.
    add(plantuml.name, "net.sourceforge.plantuml:plantuml:1.2026.8@jar")
}

val diagramSources = fileTree("UML/task-creation") { include("*.puml") }
val diagramOutput = layout.projectDirectory.dir("modules/ROOT/images/diagrams")

tasks.register<JavaExec>("renderDiagrams") {
    group = "documentation"
    description = "Render the task creation PlantUML diagrams to SVG for Antora."
    classpath = plantuml
    mainClass = "net.sourceforge.plantuml.Run"
    systemProperty("java.awt.headless", "true")
    systemProperty("PLANTUML_SECURITY_PROFILE", "SANDBOX")
    inputs.files(diagramSources).withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(diagramOutput)
    args(
        "--svg", "--no-error-image", "--disable-metadata",
        "--charset", "UTF-8",
        "--pragma", "layout=smetana",
        "--output-dir", diagramOutput.asFile.absolutePath,
    )
    args(diagramSources.files.sorted().map { it.absolutePath })
}
