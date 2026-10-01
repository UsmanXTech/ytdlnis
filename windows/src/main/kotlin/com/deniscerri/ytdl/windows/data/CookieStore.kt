package com.deniscerri.ytdl.windows.data

import com.deniscerri.ytdl.windows.model.CookieProfile
import com.deniscerri.ytdl.windows.runtime.WindowsPaths
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import java.nio.file.Files

@Serializable
private data class StoredCookieProfile(
    val id: String,
    val name: String,
    val source: String,
    val cookieFile: String?,
    val browser: String?
)

class CookieStore(
    private val file: java.nio.file.Path =
        WindowsPaths.root.resolve("data").resolve("cookies.json")
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    init {
        file.parent.createDirectories()
        if (!file.exists()) file.writeText("[]")
    }

    @Synchronized
    fun all(): List<CookieProfile> =
        runCatching {
            json.decodeFromString<List<StoredCookieProfile>>(file.readText()).map {
                CookieProfile(it.id, it.name, it.source, it.cookieFile, it.browser)
            }
        }.getOrDefault(emptyList())

    @Synchronized
    fun upsert(profile: CookieProfile) {
        val current = all().associateBy { it.id }.toMutableMap()
        current[profile.id] = profile
        file.writeText(
            json.encodeToString(
                current.values.map {
                    StoredCookieProfile(it.id, it.name, it.source, it.cookieFile, it.browser)
                }
            )
        )
    }

    @Synchronized
    fun remove(id: String) {
        val remaining = all().filterNot { it.id == id }.map {
            StoredCookieProfile(it.id, it.name, it.source, it.cookieFile, it.browser)
        }
        file.writeText(json.encodeToString(remaining))
    }
}
