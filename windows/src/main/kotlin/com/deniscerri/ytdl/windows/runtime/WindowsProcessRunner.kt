package com.deniscerri.ytdl.windows.runtime

data class ProcessResult(
    val command: List<String>,
    val exitCode: Int,
    val stdout: String,
    val stderr: String
)
