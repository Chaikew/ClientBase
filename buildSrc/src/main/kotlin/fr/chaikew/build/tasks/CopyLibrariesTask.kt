package fr.chaikew.build.tasks

import fr.chaikew.build.is64Bit
import fr.chaikew.build.isLinux
import fr.chaikew.build.isMac
import fr.chaikew.build.isWindows
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URL

open class CopyLibrariesTask: DefaultTask() {
    private val task = TaskHelper(project)

    private fun shouldRun(): Boolean =
        !(task.projectLibsDir.exists() && task.projectLibsDir.isDirectory && task.projectLibsDir.listFiles()?.isNotEmpty() == true)


    init {
        if (!shouldRun())
            outputs.upToDateWhen { true }
        else
            outputs.upToDateWhen { false }
    }

    private fun downloadLib(to: File, from: String) {
        if (!to.exists()) {
            println("Downloading: $from")
            to.parentFile.mkdirs()
            to.writeBytes(URL(from).readBytes())
        }
    }

    @TaskAction
    fun run() {
        if (!shouldRun())
            return

        var copiedLibs = 0

        task.projectLibsDir.mkdirs()

        // parse client json
        val clientJson = groovy.json.JsonSlurper().parseText(task.mcClientJson.readText()) as Map<*, *>
        (clientJson["libraries"] as ArrayList<*>).forEach { lib ->
            lib as Map<*, *>
            val downloads = lib["downloads"]!! as Map<*, *>
            val classifiers = downloads["classifiers"] as Map<*, *>?

            if (classifiers == null) {
                val artifacts = downloads["artifact"]!! as Map<*, *>

                val libPath = artifacts["path"]!! as String
                val libUrl = artifacts["url"]!! as String

                val libFile = task.projectLibsDir.resolve(File(libPath).name)
                downloadLib(libFile, libUrl)
                copiedLibs++
            } else {
                classifiers.forEach classifier@ { (classifier, classifierData) ->
                    classifierData as Map<*, *>
                    classifier as String

                    when (classifier) {
                        "natives-linux" -> if (!isLinux())
                            return@classifier

                        "natives-windows" ->
                            if (!isWindows())
                                return@classifier
                            else if (classifier.endsWith("windows-64") && !is64Bit())
                                return@classifier

                        "natives-osx" -> if (!isMac())
                            return@classifier
                    }

                    val libPath = classifierData["path"]!! as String
                    val libUrl = classifierData["url"]!! as String

                    val libFile = task.projectLibsDir.resolve(File(libPath).name)
                    downloadLib(libFile, libUrl)
                    copiedLibs++
                }
            }
        }
        println("Copied $copiedLibs libraries to \"${task.projectLibsDir.relativeTo(project.projectDir)}\"")
    }
}