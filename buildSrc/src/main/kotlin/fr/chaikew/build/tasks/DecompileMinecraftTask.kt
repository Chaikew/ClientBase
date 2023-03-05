package fr.chaikew.build.tasks

import fr.chaikew.build.isWindows
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileNotFoundException
import java.util.zip.ZipFile

open class DecompileMinecraftTask: DefaultTask() {
    private val task = TaskHelper(project)

    private fun shouldRun(): Boolean {
        val hasJavaSrc = (task.projectSrcDir.exists() && task.projectSrcDir.isDirectory && task.projectSrcDir.listFiles()?.isNotEmpty() == true)
        val hasResSrc = (task.projectResDir.exists() && task.projectResDir.isDirectory && task.projectResDir.listFiles()?.isNotEmpty() == true)
        val hasSrc = hasJavaSrc || hasResSrc

        return !hasSrc
    }

    init {
        if (!shouldRun())
            outputs.upToDateWhen { true }
        else
            outputs.upToDateWhen { false }

        if (!task.mcpZip.exists()) {
            throw RuntimeException(
                "Failed to decompile Minecraft using MCP",
                FileNotFoundException("MCP zip file (${task.mcpZip}) does not exist!")
            )
        }
    }

    @TaskAction
    fun run() {
        if (!shouldRun())
            return

        println("> Extracting MCP...")
        if (task.projectTempMCPDir.exists()) {
            task.projectTempMCPDir.deleteRecursively()
        }
        task.projectTempMCPDir.mkdirs()

        // unzip $mcpZip to tempMCP
        task.unzip(task.mcpZip.absolutePath, task.projectTempMCPDir.absolutePath)




        println("> Decompiling Minecraft using MCP (this might take some time)...")
        val decompileScript = if (isWindows()) "decompile.bat"
        else "decompile.sh"

        val decompile = project.exec {
            workingDir = task.projectTempMCPDir
            commandLine = listOf(File(task.projectTempMCPDir, decompileScript).absolutePath, "--norecompile", "--client")
        }
        decompile.assertNormalExitValue()




        val mcpDecompiledDir = task.projectTempMCPDir.resolve("src").resolve("minecraft")
        if (!mcpDecompiledDir.exists()) {
            throw RuntimeException(
                "Failed to decompile Minecraft using MCP",
                FileNotFoundException("MCP decompiled directory does not exist!")
            )
        }



        println("> Copying sources to project source directory...")
        mcpDecompiledDir.copyRecursively(task.projectSrcDir)
        task.projectSrcDir.resolve("Start.java").delete() // we don't need this file



        println("> Copying jar assets to project resources directory...")
        var copiedAssets = 0
        ZipFile(task.mcClientJar).use { libZip: ZipFile ->
            libZip.entries().asSequence().filter { !it.isDirectory }
                .filter { !it.name.endsWith(".class") }
                .filter { !it.name.startsWith("META-INF") }
                .forEach { entry ->
                    File(task.projectResDir, entry.name).parentFile.mkdirs()

                    libZip.getInputStream(entry).use { input ->
                        task.projectResDir.resolve(entry.name).outputStream().use { output ->
                            input.copyTo(output)
                            copiedAssets++
                        }
                    }
                }
        }
        println("Copied $copiedAssets assets to ${task.projectResDir.absolutePath}")



        println("> Cleaning up...")
        task.projectTempMCPDir.deleteRecursively()
    }
}