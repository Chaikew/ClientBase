package fr.chaikew.build.tasks

import fr.chaikew.build.is64Bit
import fr.chaikew.build.isLinux
import fr.chaikew.build.isMac
import fr.chaikew.build.isWindows
import org.gradle.api.tasks.*
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import java.util.zip.ZipFile
import kotlin.coroutines.coroutineContext

open class SetupWorkspaceTask: BaseTask() {
    private fun unzip(zipFilePath: String, destDir: String) {
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

    @TaskAction
    fun setupWorkspace() {
        projectNativesDir.mkdirs()
        projectLibsDir.mkdirs()
        projectSrcDir.mkdirs()
        projectResDir.mkdirs()

        fun decompileMC() {
            println("> Doing some checks before decompiling...")
            if (projectSrcDir.exists() && projectSrcDir.isDirectory && projectSrcDir.listFiles()?.isNotEmpty() == true) {
                println("Project sources directory (src/main/java) is not empty, skipping decompilation...")
                return
            }

            if (projectResDir.exists() && projectResDir.isDirectory && projectResDir.listFiles()?.isNotEmpty() == true) {
                println("Project resources directory (src/main/resources) is not empty, skipping decompilation...")
                return
            }

            if (projectLibsDir.exists() && projectLibsDir.isDirectory && projectLibsDir.listFiles()?.isNotEmpty() == true) {
                println("Project libs directory (libs) is not empty, skipping decompilation...")
                return
            }

            if (!mcpZip.exists()) {
                throw RuntimeException(
                    "Failed to decompile Minecraft using MCP",
                    FileNotFoundException("MCP zip file ($mcpZip) does not exist!")
                )
            }



            println("> Decompiling Minecraft using MCP...")
            if (projectTempMCPDir.exists()) {
                projectTempMCPDir.deleteRecursively()
            }
            projectTempMCPDir.mkdirs()

            // unzip $mcpZip to tempMCP
            unzip(mcpZip.absolutePath, projectTempMCPDir.absolutePath)

            val decompileScript = if (isWindows()) "decompile.bat"
            else "decompile.sh"

            val procbuild: ProcessBuilder = ProcessBuilder(File("tempMCP", decompileScript).absolutePath, "--norecompile", "--client")
                .directory(projectTempMCPDir)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)

            val proc = procbuild.start()

            while (proc.isAlive) {
                proc.inputStream.copyTo(System.out)
                proc.errorStream.copyTo(System.err)
                Thread.sleep(450) // wait a bit to avoid performance issues
            }

            proc.waitFor() // technically not needed, but just in case
            assert(proc.exitValue() == 0) { "Failed to decompile Minecraft using MCP" }


            val mcpDecompiledDir = projectTempMCPDir.resolve("src").resolve("minecraft")
            if (!mcpDecompiledDir.exists()) {
                throw RuntimeException(
                    "Failed to decompile Minecraft using MCP",
                    FileNotFoundException("MCP decompiled directory does not exist!")
                )
            }



            println("> Copying sources to project source directory...")
            mcpDecompiledDir.copyRecursively(projectSrcDir)
            projectSrcDir.resolve("Start.java").delete() // we don't need this file



            println("> Copying jar assets to project resources directory...")
            var copiedAssets = 0
            ZipFile(mcClientJar).use { libZip: ZipFile ->
                libZip.entries().asSequence().filter { !it.isDirectory }
                    .filter { !it.name.endsWith(".class") }
                    .filter { !it.name.startsWith("META-INF") }
                    .forEach { entry ->
                        File(projectResDir, entry.name).parentFile.mkdirs()

                        libZip.getInputStream(entry).use { input ->
                            projectResDir.resolve(entry.name).outputStream().use { output ->
                                input.copyTo(output)
                                copiedAssets++
                            }
                        }
                    }
            }
            println("Copied $copiedAssets assets to ${projectResDir.absolutePath}")



            println("> Cleaning up...")
            projectTempMCPDir.deleteRecursively()
        }


        fun copyLibs() {
            println("> Copying libraries to libs directory...")
            if (projectLibsDir.exists() && projectLibsDir.isDirectory && projectLibsDir.listFiles()?.isNotEmpty() == true) {
                println("Project libs directory (libs) is not empty, skipping libraries copy...")
                return
            }

            projectLibsDir.mkdirs()

            fun downloadLib(to: File, from: String) {
                if (!to.exists()) {
                    println("Downloading library: $from")
                    to.parentFile.mkdirs()
                    to.writeBytes(URL(from).readBytes())
                }
            }

            // parse client json
            val clientJson = groovy.json.JsonSlurper().parseText(mcClientJson.readText()) as Map<*, *>
            (clientJson["libraries"] as ArrayList<*>).forEach { lib ->
                lib as Map<*, *>
                val downloads = lib["downloads"]!! as Map<*, *>
                val classifiers = downloads["classifiers"] as Map<*, *>?

                if (classifiers == null) {
                    val artifacts = downloads["artifact"]!! as Map<*, *>

                    val libPath = artifacts["path"]!! as String
                    val libUrl = artifacts["url"]!! as String

                    val libFile = projectLibsDir.resolve(File(libPath).name)
                    downloadLib(libFile, libUrl)
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

                        val libFile = projectLibsDir.resolve(File(libPath).name)
                        downloadLib(libFile, libUrl)
                    }
                }
            }
        }


        fun copyNatives() {
            println("> Copying native libraries...")
            var copiedNatives = 0

            if (projectNativesDir.exists() && projectNativesDir.isDirectory && projectNativesDir.listFiles()?.isNotEmpty() == true) {
                println("Project natives directory (natives) is not empty, skipping natives copy...")
                return
            }
            projectNativesDir.mkdirs()

            val libs = project.fileTree(projectLibsDir).filter { it.name.endsWith(".jar") }

            libs.forEach { libFile ->
                ZipFile(libFile).use { libZip ->
                    libZip.entries().asSequence().filter { !it.isDirectory }
                        .filter { it.name.endsWith(".dll") || it.name.endsWith(".so") || it.name.endsWith(".dylib") }
                        .forEach { entry ->
                            File(projectNativesDir, entry.name).parentFile.mkdirs()

                            libZip.getInputStream(entry).use { input ->
                                projectNativesDir.resolve(entry.name).outputStream().use { output ->
                                    input.copyTo(output)
                                    copiedNatives++
                                }
                            }
                        }
                }
            }

            println("Copied $copiedNatives natives to ${projectNativesDir.absolutePath}")
        }

        decompileMC()
        copyLibs()
        copyNatives()
    }
}