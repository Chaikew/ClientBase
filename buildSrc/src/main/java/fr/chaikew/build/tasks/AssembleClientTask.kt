package fr.chaikew.build.tasks

import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.StandardCopyOption

open class AssembleClientTask: BaseTask() {
    init {
        project.tasks.getByName("build").finalizedBy(this)
    }
    @TaskAction
    public fun run() {
        val outputDir = project.buildDir.resolve("libs")
        val gradleGeneratedJar = outputDir.resolve("${project.name}-${project.version}.jar")

        // copy client.json
        val outputClientJson = outputDir.resolve("${project.name}.json")
        Files.copy(mcClientJson.toPath(), outputClientJson.toPath(), StandardCopyOption.REPLACE_EXISTING)

        // edit it
        val clientJson = (groovy.json.JsonSlurper().parseText(outputClientJson.readText()) as Map<*, *>).toMutableMap()
        // disable integrity check
        val downloads = (clientJson["downloads"] as Map<*, *>).toMutableMap()
        downloads.remove("client")
        clientJson["downloads"] = downloads

        // set new name
        clientJson["id"] = project.name

        // copy the jar
        gradleGeneratedJar.renameTo(outputDir.resolve("${project.name}.jar"))
    }
}