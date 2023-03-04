package fr.chaikew.build

import java.util.*

fun osName(): String = System.getProperty("os.name").toLowerCase()
fun isWindows(): Boolean = osName().contains("win")
fun isLinux(): Boolean = osName().contains("linux")
fun isMac(): Boolean = osName().contains("mac")
fun is64Bit(): Boolean = System.getProperty("os.arch").contains("64")