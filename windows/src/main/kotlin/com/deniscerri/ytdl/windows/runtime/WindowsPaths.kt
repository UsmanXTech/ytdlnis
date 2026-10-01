package com.deniscerri.ytdl.windows.runtime

import java.nio.file.Path
import java.nio.file.Paths

object WindowsPaths {
    private val localAppData = System.getenv("LOCALAPPDATA")
        ?.takeIf { it.isNotBlank() }
        ?.let(Paths::get)
        ?: Paths.get(System.getProperty("user.home"), "AppData", "Local")

    val root: Path = localAppData.resolve("YTDLnis")
    val runtime: Path = root.resolve("runtime")
    val ytDlp: Path = root.resolve("yt-dlp")
    val plugins: Path = root.resolve("plugins")
    val cache: Path = root.resolve("cache")
    val logs: Path = root.resolve("logs")
}
