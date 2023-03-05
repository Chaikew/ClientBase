package fr.chaikew.build.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class SetupWorkspaceTask : DefaultTask() {
    private val task = TaskHelper(project)


    init {
        this.finalizedBy(
            project.tasks.getByName(TASK_DECOMPILE_MC).finalizedBy(
                project.tasks.getByName(TASK_COPY_LIBS).finalizedBy(
                    project.tasks.getByName(TASK_COPY_NATIVES)
                )
            )
        )
    }
}