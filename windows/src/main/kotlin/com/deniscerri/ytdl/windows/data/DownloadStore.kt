package com.deniscerri.ytdl.windows.data

import com.deniscerri.ytdl.windows.model.DownloadEntry
import com.deniscerri.ytdl.windows.model.DownloadStatus
import com.deniscerri.ytdl.windows.runtime.WindowsPaths
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

@Serializable
private data class StoredDownload(
    val id: String,
    val url: String,
    val title: String,
    val outputDir: String,
    val status: String,
    val progress: Float,
    val etaSeconds: Long,
    val message: String,
    val createdAt: Long,
    val completedAt: Long?
)

class DownloadStore(
    private val file: Path = WindowsPaths.root.resolve("data").resolve("downloads.json")
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    init {
        file.parent.createDirectories()
        if (!Files.exists(file)) file.writeText("[]")
    }

    @Synchronized
    fun all(): List<DownloadEntry> =
        runCatching {
            json.decodeFromString<List<StoredDownload>>(file.readText()).map(::toEntry)
        }.getOrDefault(emptyList())

    @Synchronized
    fun upsert(entry: DownloadEntry) {
        val current = all().associateBy { it.id }.toMutableMap()
        current[entry.id] = entry
        file.writeText(json.encodeToString(current.values.sortedByDescending { it.createdAt }.map(::fromEntry)))
    }

    @Synchronized
    fun remove(id: String) {
        file.writeText(json.encodeToString(all().filterNot { it.id == id }.map(::fromEntry)))
    }

    private fun fromEntry(value: DownloadEntry) = StoredDownload(
        value.id,
        value.url,
        value.title,
        value.outputDir,
        value.status.name,
        value.progress,
        value.etaSeconds,
        value.message,
        value.createdAt,
        value.completedAt
    )

    private fun toEntry(value: StoredDownload) = DownloadEntry(
        value.id,
        value.url,
        value.title,
        value.outputDir,
        runCatching { DownloadStatus.valueOf(value.status) }.getOrDefault(DownloadStatus.QUEUED),
        value.progress,
        value.etaSeconds,
        value.message,
        value.createdAt,
        value.completedAt
    )
}
