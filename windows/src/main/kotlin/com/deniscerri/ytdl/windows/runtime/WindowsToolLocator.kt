package com.deniscerri.ytdl.windows.runtime

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class WindowsToolLocator(
    private val paths: WindowsPaths = WindowsPaths
) {
    fun find(spec: ToolSpec, environmentVariable: String? = null): Path? {
        if (environmentVariable != null) {
            System.getenv(environmentVariable)
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    val path = Paths.get(it)
                    if (Files.isRegularFile(path)) return path
                }
        }

        val bundled = paths.runtime.resolve(spec.directoryName).resolve(spec.executableName)
        if (Files.isRegularFile(bundled)) return bundled

        val root = if (spec.name == "yt-dlp") {
            paths.ytDlp.resolve(spec.executableName)
        } else {
            paths.runtime.resolve(spec.directoryName).resolve(spec.executableName)
        }
        if (Files.isRegularFile(root)) return root

        val separator = System.getProperty("path.separator")
        return System.getenv("PATH").orEmpty()
            .split(separator)
            .filter(String::isNotBlank)
            .asSequence()
            .map { Paths.get(it).resolve(spec.executableName) }
            .firstOrNull { Files.isRegularFile(it) }
    }

    fun require(spec: ToolSpec, environmentVariable: String? = null): Path =
        find(spec, environmentVariable)
            ?: error(spec.name + " executable was not found")
}
