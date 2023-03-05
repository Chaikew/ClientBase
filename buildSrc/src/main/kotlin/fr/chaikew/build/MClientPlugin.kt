package fr.chaikew.build

import fr.chaikew.build.tasks.*

class MClientPlugin: org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(project: org.gradle.api.Project) {
        TaskHelper(project).getLibs().forEach {
            project.dependencies.add("implementation", project.files(it))
        }

        project.tasks.register(TASK_SETUP_WORKSPACE, SetupWorkspaceTask::class.java)
        project.tasks.register(TASK_DECOMPILE_MC, DecompileMinecraftTask::class.java)
        project.tasks.register(TASK_COPY_NATIVES, CopyNativesTask::class.java)
        project.tasks.register(TASK_COPY_LIBS, CopyLibrariesTask::class.java)

        project.tasks.register(TASK_ASSEMBLE_CLIENT, AssembleClientTask::class.java)
        project.tasks.register(TASK_RUN_CLIENT, RunClientTask::class.java)

        project.tasks.register(TASK_CLEAN_PROJECT, CleanProjectTask::class.java)
    }
}