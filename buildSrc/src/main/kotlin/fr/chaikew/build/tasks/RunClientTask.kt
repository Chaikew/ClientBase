package fr.chaikew.build.tasks

import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction
import java.io.File

open class RunClientTask: JavaExec() {
    private val task = TaskHelper(project)

    init {
        this.dependsOn(project.tasks.getByName("build"))

        group = "Minecraft Client"
        description = "Runs the Minecraft client"
        mainClass.set("net.minecraft.client.main.Main")
        classpath = task.mainSourceSet.runtimeClasspath
        workingDir = File("run")

        jvmArgs = listOf(
            "-Djava.library.path=${task.projectNativesDir.absolutePath}"
        )

        args = listOf(
            // Minecraft arguments
            "--version", "mcp",
            "--accessToken", "0",
            "--assetsDir", task.getMinecraftHome().resolve("assets").absolutePath, // this avoids copying the assets
            "--assetIndex", "1.8",
            "--userProperties", "{}",
            "--gameDir", workingDir.absolutePath
        )
    }
}