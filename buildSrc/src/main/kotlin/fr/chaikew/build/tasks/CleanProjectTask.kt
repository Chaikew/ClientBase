package fr.chaikew.build.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskAction

open class CleanProjectTask: Delete() {
    private val task = TaskHelper(project)

    init {
        this.dependsOn(project.tasks.getByName("clean"))

        this.delete(task.projectNativesDir)
        this.delete(task.projectLibsDir)
        this.delete(task.projectTempMCPDir)
    }
}