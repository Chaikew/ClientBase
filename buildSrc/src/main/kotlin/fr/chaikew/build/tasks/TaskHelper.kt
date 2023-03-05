package fr.chaikew.build.tasks

import fr.chaikew.build.osName
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*
import java.io.File
import java.util.zip.ZipFile

class TaskHelper(private val project: Project) {
    val sourceSets: SourceSetContainer = project.extensions.getByType(SourceSetContainer::class.java)
    val mainSourceSet: SourceSet = sourceSets.getByName("main")

    val mcClientVersion: String = project.properties.getOrDefault("mcClientVersion", "1.8.9") as String
    val mcClientJar: File = getMinecraftHome().resolve("versions").resolve(mcClientVersion).resolve("$mcClientVersion.jar")
    val mcClientJson: File = getMinecraftHome().resolve("versions").resolve(mcClientVersion).resolve("$mcClientVersion.json")

    val mcpZip: File = File(project.properties.getOrDefault("mcpZip", "mcp.zip") as String)

    val projectLibsDir: File = project.projectDir.resolve("libs")
    val projectNativesDir: File = project.projectDir.resolve("natives")
    val projectSrcDir: File = mainSourceSet.java.srcDirs.first()
    val projectResDir: File = mainSourceSet.resources.srcDirs.first()
    val projectTempMCPDir: File = project.projectDir.resolve("tempMCP")

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

    fun getLibs(): FileCollection {
        return project.fileTree(project.projectDir.resolve("libs")).filter { it.name.endsWith(".jar") }
    }

    fun unzip(zipFilePath: String, destDir: String) {
        ZipFile(zipFilePath).use { zip ->
            zip.entries().asSequence()
                .filter { !it.isDirectory }
                .forEach { entry ->
                    zip.getInputStream(entry).use { input ->
                        val file = File(destDir, entry.name)
                        file.parentFile.mkdirs()
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
        }
    }
}