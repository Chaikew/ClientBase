package fr.chaikew.build

import fr.chaikew.build.tasks.*
import fr.chaikew.build.tasks.BaseTask
import fr.chaikew.build.tasks.SetupWorkspaceTask
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies

class MClientPlugin: org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(project: org.gradle.api.Project) {
        BaseTask.getLibs(project).forEach {
            project.dependencies.add("compileOnly", project.files(it))
        }

        project.tasks.register("setupWorkspace", SetupWorkspaceTask::class.java)
        project.tasks.register("assembleClient", AssembleClientTask::class.java)
        project.tasks.register("runClient", RunClientTask::class.java)
        project.tasks.register("cleanProject", CleanProjectTask::class.java)

    }
}