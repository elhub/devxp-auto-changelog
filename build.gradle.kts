description = "Automated changelog generation for git projects"

subprojects {
    group = if (this.name == "cli") "" else parent?.group?.toString() ?: "no.elhub.devxp"
    version = rootProject.version
    val subproject = this@subprojects

    dependencies {
        val jgitVersion = "5.11.0.202103091610-r"
        implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.0")
        implementation("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")
        implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:$jgitVersion")
    }

}

tasks.withType(Jar::class.java) {
    enabled = false // nothing to build in the root project
}
