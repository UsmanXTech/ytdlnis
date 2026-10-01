package com.deniscerri.ytdl.windows.runtime

import com.deniscerri.ytdl.windows.engine.ProgressParser
import com.deniscerri.ytdl.windows.engine.ProgressUpdate
import java.io.File
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
        workingDirectory: File? = null,
        environment: Map<String, String> = emptyMap(),
        onOutput: ((ProgressUpdate) -> Unit)? = null
    ): ProcessResult {
        require(command.isNotEmpty()) { "Command cannot be empty" }

        val builder = ProcessBuilder(command)
            .redirectErrorStream(true)
        workingDirectory?.let(builder::directory)
        builder.environment().putAll(environment)

        val process = builder.start()
        val parser = ProgressParser()
        val output = StringBuilder()

        process.inputStream.bufferedReader(StandardCharsets.UTF_8).use { reader ->
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
