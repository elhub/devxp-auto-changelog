import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.application)
    alias(libs.plugins.shadow)
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
                "Main-Class" to "no/elhub/devxp/autochangelog/cli/AutoChangelog.kt"
            )
        )
    }
    from(project.configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    mergeServiceFiles("META-INF/cxf/bus-extensions.txt")
    with(tasks.jar.get() as CopySpec)
}

afterEvaluate {
    publishing {
        publications {
            all {
                this as MavenPublication
                if (name == rootProject.name) {
                    artifacts.clear()
                    artifact(shadowJar)

                }
            }
        }
    }
}

tasks.artifactoryPublish {
    dependsOn(shadowJar)
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
