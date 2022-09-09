plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("application")
}

dependencies {
    implementation(project(":core"))
    implementation("info.picocli:picocli:4.6.3")
}

group = ""

val applicationMainClass : String by project

application {
    mainClass.set(applicationMainClass)
}

val shadowJar by tasks.getting(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
    archiveBaseName.set(rootProject.name)
    archiveClassifier.set("")
}

tasks["assemble"].dependsOn(tasks["shadowJar"])
