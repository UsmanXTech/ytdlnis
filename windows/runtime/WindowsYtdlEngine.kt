package com.deniscerri.ytdl.windows.runtime

import com.deniscerri.ytdl.windows.engine.ProgressUpdate
import com.deniscerri.ytdl.windows.engine.YtdlRequest
import java.nio.file.Files
import java.nio.file.Path

class WindowsYtdlEngine(
    private val runtime: WindowsRuntime = WindowsRuntime(),
    private val locator: WindowsToolLocator = WindowsToolLocator(runtime.paths)
) {
    private val registry = WindowsProcessRegistry()

    fun initialize() = runtime.initialize()

    fun execute(
        request: YtdlRequest,
        processId: String? = null,
        useFfmpeg: Boolean = true,
        onOutput: ((ProgressUpdate) -> Unit)? = null
    ): ProcessResult {
        initialize()

        val ytDlp = locator.require(WindowsTools.ytDlp, "YTDLNIS_YTDLP")
        val ffmpeg = locator.find(WindowsTools.ffmpeg, "YTDLNIS_FFMPEG")

        val command = buildList {
            add(ytDlp.toString())
            if (useFfmpeg && ffmpeg != null) {
                add("--ffmpeg-location")
                add(ffmpeg.parent.toString())
            }
            addAll(request.buildCommand())
        }

        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .apply { environment().putAll(runtime.environment()) }
            .start()

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
            ProcessResult(command, exitCode, output.toString(), "")
        } finally {
            if (processId != null) registry.remove(processId)
        }
    }

    fun ffmpegPath(): Path? = locator.find(WindowsTools.ffmpeg, "YTDLNIS_FFMPEG")
    fun ffprobePath(): Path? = locator.find(WindowsTools.ffprobe, "YTDLNIS_FFMPEG")
    fun hasAria2c(): Boolean = locator.find(WindowsTools.aria2c, "YTDLNIS_ARIA2C") != null
    fun cancel(processId: String): Boolean = registry.cancel(processId)
}
