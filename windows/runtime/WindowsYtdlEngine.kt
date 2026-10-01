package com.deniscerri.ytdl.windows.runtime

import com.deniscerri.ytdl.windows.engine.ProgressUpdate
import com.deniscerri.ytdl.windows.engine.YtdlRequest
import java.nio.file.Files

class WindowsYtdlEngine(
    private val runtime: WindowsRuntime = WindowsRuntime()
) {
    private val registry = WindowsProcessRegistry()

    fun initialize() = runtime.initialize()

    fun locateYtDlp(): java.nio.file.Path {
        val configured = System.getenv("YTDLNIS_YTDLP")
        if (!configured.isNullOrBlank()) return java.nio.file.Paths.get(configured)
        val local = runtime.ytDlpExecutable()
        if (Files.isRegularFile(local)) return local
        val onPath = java.nio.file.Paths.get("yt-dlp.exe")
        if (Files.isRegularFile(onPath)) return onPath
        error("yt-dlp.exe was not found. Place it in %LOCALAPPDATA%\\YTDLnis\\yt-dlp\\yt-dlp.exe or set YTDLNIS_YTDLP.")
    }

    fun execute(
        request: YtdlRequest,
        processId: String? = null,
        onOutput: ((ProgressUpdate) -> Unit)? = null
    ): ProcessResult {
        initialize()
        val command = listOf(locateYtDlp().toString()) + request.buildCommand()
        val process = ProcessBuilder(command).redirectErrorStream(true).apply {
            environment().putAll(runtime.environment())
        }.start()
        if (processId != null) registry.register(processId, process)
        val output = StringBuilder()
        val parser = com.deniscerri.ytdl.windows.engine.ProgressParser()
        return try {
            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    output.appendLine(line)
                    onOutput?.invoke(parser.parse(line))
                }
            }
            val exitCode = process.waitFor()
            if (exitCode != 0) throw RuntimeException("yt-dlp exited with code $exitCode\\n$output")
            ProcessResult(command, exitCode, output.toString(), "")
        } finally {
            if (processId != null) registry.remove(processId)
        }
    }

    fun cancel(processId: String): Boolean = registry.cancel(processId)
}
