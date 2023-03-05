package fr.chaikew.build.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.zip.ZipFile

open class CopyNativesTask : DefaultTask() {
    private val task = TaskHelper(project)

    private fun shouldRun(): Boolean =
        !(task.projectNativesDir.exists() && task.projectNativesDir.isDirectory && task.projectNativesDir.listFiles()?.isNotEmpty() == true)

    init {
        if (!shouldRun())
            outputs.upToDateWhen { true }
        else
            outputs.upToDateWhen { false }
    }

    @TaskAction
    fun run() {
        if (!shouldRun())
            return

        if (task.projectNativesDir.exists())
            task.projectNativesDir.deleteRecursively()
        task.projectNativesDir.mkdirs()

        var copiedNatives = 0

        val libs = project.fileTree(task.projectLibsDir).filter { it.name.endsWith(".jar") }

        libs.forEach { libFile ->
            ZipFile(libFile).use { libZip ->
                libZip.entries().asSequence().filter { !it.isDirectory }
                    .filter { it.name.endsWith(".dll") || it.name.endsWith(".so") || it.name.endsWith(".dylib") }
                    .forEach { entry ->
                        File(task.projectNativesDir, entry.name).parentFile.mkdirs()

                        libZip.getInputStream(entry).use { input ->
                            task.projectNativesDir.resolve(entry.name).outputStream().use { output ->
                                input.copyTo(output)
                                copiedNatives++
                            }
                        }
                    }
            }
        }

        println("Copied $copiedNatives natives to \"${task.projectNativesDir.relativeTo(project.projectDir)}\"")
    }
}