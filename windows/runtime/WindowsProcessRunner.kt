package com.deniscerri.ytdl.windows.runtime

import com.deniscerri.ytdl.windows.engine.ProgressParser
import com.deniscerri.ytdl.windows.engine.ProgressUpdate
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

data class ProcessResult(
    val command: List<String>,
    val exitCode: Int,
    val stdout: String,
    val stderr: String
)

class WindowsProcessRunner {
    fun run(
        command: List<String>,
        workingDirectory: java.io.File? = null,
        onOutput: ((ProgressUpdate) -> Unit)? = null
    ): ProcessResult {
        require(command.isNotEmpty()) { "Command cannot be empty" }

        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .apply { workingDirectory?.let(::directory) }
            .start()

        val parser = ProgressParser()
        val output = StringBuilder()

        BufferedReader(InputStreamReader(process.inputStream, StandardCharsets.UTF_8)).use { reader ->
            while (true) {
                val line = reader.readLine() ?: break
                output.appendLine(line)
                onOutput?.invoke(parser.parse(line))
            }
        }

        val exitCode = process.waitFor()
        return ProcessResult(command, exitCode, output.toString(), "")
    }
}
