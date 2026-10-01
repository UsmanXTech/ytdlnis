package com.deniscerri.ytdl.windows.runtime

import com.deniscerri.ytdl.windows.engine.YtdlRequest
import java.nio.file.Files

class WindowsYtdlEngine(
    private val paths: WindowsPaths = WindowsPaths
) {
    private val runner = WindowsProcessRunner()

    fun locateYtDlp(): java.nio.file.Path {
        val configured = System.getenv("YTDLNIS_YTDLP")
        if (!configured.isNullOrBlank()) return java.nio.file.Paths.get(configured)

        val local = paths.ytDlp.resolve("yt-dlp.exe")
        if (Files.isRegularFile(local)) return local

        val onPath = java.nio.file.Paths.get("yt-dlp.exe")
        if (Files.isRegularFile(onPath)) return onPath

        error("yt-dlp.exe was not found. Place it in %LOCALAPPDATA%\\YTDLnis\\yt-dlp\\yt-dlp.exe or set YTDLNIS_YTDLP.")
    }

    fun execute(
        request: YtdlRequest,
        onOutput: ((com.deniscerri.ytdl.windows.engine.ProgressUpdate) -> Unit)? = null
    ): ProcessResult {
        val ytDlp = locateYtDlp()
        return runner.run(listOf(ytDlp.toString()) + request.buildCommand(), onOutput = onOutput)
    }
}
