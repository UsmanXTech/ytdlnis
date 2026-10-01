package com.deniscerri.ytdl.windows.data

import com.deniscerri.ytdl.windows.model.TemplateEntry
import com.deniscerri.ytdl.windows.runtime.WindowsPaths
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@Serializable
private data class StoredTemplate(
    val id: String,
    val title: String,
    val command: String
)

class TemplateStore(
    private val file: java.nio.file.Path =
        WindowsPaths.root.resolve("data").resolve("templates.json")
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    init {
        file.parent.createDirectories()
        if (!file.exists()) file.writeText("[]")
    }

    @Synchronized
    fun all(): List<TemplateEntry> =
        runCatching {
            json.decodeFromString<List<StoredTemplate>>(file.readText())
                .map { TemplateEntry(it.id, it.title, it.command) }
        }.getOrDefault(emptyList())

    @Synchronized
    fun upsert(template: TemplateEntry) {
        val current = all().associateBy { it.id }.toMutableMap()
        current[template.id] = template
        file.writeText(
            json.encodeToString(
                current.values.map { StoredTemplate(it.id, it.title, it.command) }
            )
        )
    }

    @Synchronized
    fun remove(id: String) {
        file.writeText(
            json.encodeToString(
                all().filterNot { it.id == id }
                    .map { StoredTemplate(it.id, it.title, it.command) }
            )
        )
    }
}
