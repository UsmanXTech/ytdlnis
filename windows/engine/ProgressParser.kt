package com.deniscerri.ytdl.windows.engine

import java.util.regex.Pattern

data class ProgressUpdate(
    val progress: Float,
    val etaSeconds: Long,
    val line: String
)

class ProgressParser {
    private val ytDlp = Pattern.compile("\\[download\\]\\s+(\\d+\\.\\d)% .* ETA (\\d+):(\\d+)")
    private val aria2c = Pattern.compile("\\[#\\w{6}.*\\((\\d*\\.*\\d+)%\\).*?((\\d+)m)*((\\d+)s)*]")
    private var progress = -1f
    private var eta = -1L

    fun parse(line: String): ProgressUpdate {
        val yt = ytDlp.matcher(line)
        if (yt.find()) {
            progress = yt.group(1)!!.toFloat()
            eta = yt.group(2)!!.toLong() * 60 + yt.group(3)!!.toLong()
        } else {
            val aria = aria2c.matcher(line)
            if (aria.find()) {
                progress = aria.group(1)!!.toFloat()
                val minutes = aria.group(3)?.toLongOrNull() ?: 0L
                val seconds = aria.group(5)?.toLongOrNull() ?: 0L
                eta = minutes * 60 + seconds
            }
        }
        return ProgressUpdate(progress, eta, line)
    }
}
