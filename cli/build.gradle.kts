dependencies {
    implementation(project(":core"))
    implementation("info.picocli:picocli:4.6.1")
}

group = ""

val mainClassName: String by project

val fatJar by tasks.creating(type = Jar::class) {
    manifest {
        attributes["Main-Class"] = mainClassName
    }
    from(configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "*.html")
    with(tasks.jar.get() as CopySpec)
    mustRunAfter(tasks["jar"])
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks["assemble"].dependsOn("fatJar")
