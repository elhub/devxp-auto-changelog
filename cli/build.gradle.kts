plugins {
    id("no.elhub.devxp.kotlin-application")
}

dependencies {
    implementation(project(":core"))
    implementation(libs.cli.picocli)
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
