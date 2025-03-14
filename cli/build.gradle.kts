import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("application")
    id("com.gradleup.shadow") version "8.3.6"
}

dependencies {
    implementation(project(":core"))
    implementation(libs.cli.picocli)
}

val applicationMainClass: String by project

application {
    mainClass.set(applicationMainClass)
}

tasks.jar { enabled = false }

val shadowJar by tasks.getting(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
    archiveBaseName.set(rootProject.name)
    archiveClassifier.set("")
    isZip64 = true
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Main-Class" to applicationMainClass
            )
        )
    }
    from(project.configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    mergeServiceFiles("META-INF/cxf/bus-extensions.txt")
    with(tasks.jar.get() as CopySpec)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        javaParameters = true
    }
}
