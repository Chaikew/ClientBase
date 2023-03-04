package fr.chaikew.build.tasks

import fr.chaikew.build.isWindows
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Paths

open class RunClientTask: BaseTask() {
    init {
        this.dependsOn(project.tasks.getByName("build"))
    }

    @TaskAction
    public fun run() {
        val classPathJoiner = if (isWindows()) ";"
        else ":"


        val javaExe = Paths.get(
            System.getProperty("java.home"), "bin", if (isWindows()) "java.exe"
            else "java"
        ).toFile().absolutePath


        val libs = project.configurations.getByName("compileClasspath").filter { it.name.endsWith(".jar") }
        val classesOutputDir = mainSourceSet.output.classesDirs
        val resourcesOutputDir = mainSourceSet.output.resourcesDir

        val classpath = arrayListOf<String>()
        classpath.addAll(libs.map { it.absolutePath })
        classpath.addAll(classesOutputDir.map { it.absolutePath })
        resourcesOutputDir?.let {
            classpath.add(it.absolutePath)
        }

        val nativesDir = File("natives").absolutePath

        val mainClass = "net.minecraft.client.main.Main"


        val proc = ProcessBuilder(
            javaExe,
            "-cp",
            classpath.joinToString(classPathJoiner),
            "-Djava.library.path=$nativesDir",
            mainClass,

            // Minecraft arguments
            "--version", "mcp",
            "--accessToken", "0",
            "--assetsDir", getMinecraftHome().resolve("assets").absolutePath, // this avoids copying the assets
            "--assetIndex", "1.8",
            "--userProperties", "{}",
            "--gameDir", File(".", "run").absolutePath,
        ).start()
        proc.waitFor()
        println("Minecraft exited with code ${proc.exitValue()}")
    }

}