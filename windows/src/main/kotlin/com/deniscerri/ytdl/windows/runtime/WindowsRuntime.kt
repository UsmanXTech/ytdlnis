package com.deniscerri.ytdl.windows.runtime

import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class WindowsRuntime(
    private val paths: WindowsPaths = WindowsPaths
) {
    fun initialize() {
        listOf(paths.root, paths.runtime, paths.ytDlp, paths.plugins, paths.cache, paths.logs)
            .forEach { Files.createDirectories(it) }
    }

    fun ytDlpExecutable(): Path = paths.ytDlp.resolve("yt-dlp.exe")
    fun ffmpegExecutable(): Path = paths.runtime.resolve("ffmpeg").resolve("bin").resolve("ffmpeg.exe")

    fun environment(): Map<String, String> {
        val env = linkedMapOf<String, String>()
        env["HOME"] = paths.root.toString()
        env["TEMP"] = System.getProperty("java.io.tmpdir")
        env["TMP"] = System.getProperty("java.io.tmpdir")
        val runtimeBin = paths.runtime.resolve("bin").toString()
        val existingPath = System.getenv("PATH").orEmpty()
        env["PATH"] = if (existingPath.isBlank()) runtimeBin else runtimeBin + File.pathSeparator + existingPath
        return env
    }
}
