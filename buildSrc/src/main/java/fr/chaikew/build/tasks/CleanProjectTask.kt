package fr.chaikew.build.tasks

import org.gradle.api.tasks.TaskAction

open class CleanProjectTask: BaseTask() {
    init {
        this.dependsOn(project.tasks.getByName("clean"))
    }

    @TaskAction
    fun cleanProject() {
        if (projectNativesDir.exists())
            projectNativesDir.deleteRecursively()
        if (projectLibsDir.exists())
            projectLibsDir.deleteRecursively()
        if (projectTempMCPDir.exists())
            projectTempMCPDir.deleteRecursively()
    }
}