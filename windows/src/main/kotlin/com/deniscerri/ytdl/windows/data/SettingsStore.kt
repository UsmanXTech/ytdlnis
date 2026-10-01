package com.deniscerri.ytdl.windows.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.deniscerri.ytdl.windows.runtime.WindowsPaths
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@Serializable
data class WindowsSettings(
    val defaultOutputDirectory: String = "",
    val defaultFormat: String = "bestvideo+bestaudio/best",
    val maxConcurrentDownloads: Int = 2,
    val embedMetadata: Boolean = true,
    val embedThumbnail: Boolean = false,
    val useAria2c: Boolean = false,
    val useCookiesFromBrowser: Boolean = false,
    val browser: String = ""
)

class SettingsStore(
    private val file: java.nio.file.Path =
        WindowsPaths.root.resolve("data").resolve("settings.json")
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    init {
        file.parent.createDirectories()
        if (!file.exists()) file.writeText(json.encodeToString(WindowsSettings()))
    }

    @Synchronized
    fun load(): WindowsSettings =
        runCatching { json.decodeFromString<WindowsSettings>(file.readText()) }
            .getOrDefault(WindowsSettings())

    @Synchronized
    fun save(settings: WindowsSettings) {
        file.writeText(json.encodeToString(settings))
    }
}
