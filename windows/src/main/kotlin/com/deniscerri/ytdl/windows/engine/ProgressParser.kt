package com.deniscerri.ytdl.windows.engine

import java.util.regex.Pattern

data class ProgressUpdate(
    val progress: Float,
    val etaSeconds: Long,
    val line: String
)

class ProgressParser {
    private val ytDlp = Pattern.compile("\\[download\\].*?(\\d+(?:\\.\\d+)?)%.*?ETA\\s+(?:(\\d+):)?(\\d+):(\\d+)")
    private val aria2c = Pattern.compile("\\[#\\w{6}.*\\((\\d+(?:\\.\\d+)?)%\\).*")

    private var progress = -1f
    private var eta = -1L

    fun parse(line: String): ProgressUpdate {
        val yt = ytDlp.matcher(line)
        if (yt.find()) {
            progress = yt.group(1)?.toFloatOrNull() ?: progress
            val hours = yt.group(2)?.toLongOrNull() ?: 0L
            val minutes = yt.group(3)?.toLongOrNull() ?: 0L
            val seconds = yt.group(4)?.toLongOrNull() ?: 0L
            eta = hours * 3600 + minutes * 60 + seconds
        } else {
            val aria = aria2c.matcher(line)
            if (aria.find()) progress = aria.group(1)?.toFloatOrNull() ?: progress
        }
        return ProgressUpdate(progress, eta, line)
    }
}
