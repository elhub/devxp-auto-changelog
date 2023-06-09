plugins {
}

dependencies {
    implementation(project(":core"))
    implementation(libs.cli.picocli)
}

group = ""

val applicationMainClass : String by project

val fatJar by tasks.creating(type = Jar::class) {
    manifest {
        attributes["Implementation-Title"] = rootProject.name
        attributes["Implementation-Version"] = rootProject.version
        attributes["Main-Class"] = applicationMainClass
    }
    from(configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "*.html")
    with(tasks.jar.get() as CopySpec)
    mustRunAfter(tasks["jar"])
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks["assemble"].dependsOn("fatJar")

tasks["generateMetadataFileForCliPublication"].dependsOn("fatJar")
