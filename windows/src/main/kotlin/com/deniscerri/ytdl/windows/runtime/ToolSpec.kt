package com.deniscerri.ytdl.windows.runtime

data class ToolSpec(
    val name: String,
    val executableName: String,
    val directoryName: String
)

object WindowsTools {
    val ytDlp = ToolSpec("yt-dlp", "yt-dlp.exe", "yt-dlp")
    val ffmpeg = ToolSpec("FFmpeg", "ffmpeg.exe", "ffmpeg")
    val ffprobe = ToolSpec("FFprobe", "ffprobe.exe", "ffmpeg")
    val aria2c = ToolSpec("Aria2c", "aria2c.exe", "aria2")
    val node = ToolSpec("Node.js", "node.exe", "node")
    val deno = ToolSpec("Deno", "deno.exe", "deno")
    val quickJs = ToolSpec("QuickJS", "qjs.exe", "quickjs")
}
