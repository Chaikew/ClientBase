package fr.chaikew.build.tasks

import org.gradle.api.DefaultTask

open class SetupWorkspaceTask : DefaultTask() {
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