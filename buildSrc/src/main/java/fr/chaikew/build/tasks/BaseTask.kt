package fr.chaikew.build.tasks

import fr.chaikew.build.osName
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SourceSetContainer
import java.io.File

abstract class BaseTask: DefaultTask() {
    @Internal
    val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
    @Internal
    val mainSourceSet = sourceSets.getByName("main")

    @Internal
    val mcClientVersion: String = project.properties.getOrDefault("mcClientVersion", "1.8.9") as String
    @Internal
    val mcClientJar: File = getMinecraftHome().resolve("versions").resolve(mcClientVersion).resolve("$mcClientVersion.jar")
    @Internal
    val mcClientJson: File = getMinecraftHome().resolve("versions").resolve(mcClientVersion).resolve("$mcClientVersion.json")

    @Internal
    val mcpZip: File = File(project.properties.getOrDefault("mcpZip", "mcp.zip") as String)

    @Internal
    val projectLibsDir: File = project.projectDir.resolve("libs")
    @Internal
    val projectNativesDir: File = project.projectDir.resolve("natives")
    @Internal
    val projectSrcDir: File = mainSourceSet.java.srcDirs.first()
    @Internal
    val projectResDir: File = mainSourceSet.resources.srcDirs.first()
    @Internal
    val projectTempMCPDir: File = project.projectDir.resolve("tempMCP")

    @Internal
    fun getMinecraftHome(): File {
        val osName = osName()
        return if (osName.contains("win")) {
            File(System.getenv("APPDATA"), ".minecraft")
        } else if (osName.contains("mac")) {
            File(System.getProperty("user.home"), "Library/Application Support/minecraft")
        } else {
            File(System.getProperty("user.home"), ".minecraft")
        }
    }

    companion object {
        @Internal
        fun getLibs(project: Project): FileCollection {
            return project.fileTree(project.projectDir.resolve("libs")).filter { it.name.endsWith(".jar") }
        }
    }

    @Internal
    fun getLibs(): FileCollection {
        return getLibs(project)
    }
}