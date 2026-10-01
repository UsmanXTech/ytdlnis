package com.deniscerri.ytdl.windows.runtime

import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipInputStream

class WindowsRuntimeBootstrap(
    private val paths: WindowsPaths = WindowsPaths,
    private val client: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()
) {
    fun initializeDirectories() {
        WindowsRuntime(paths).initialize()
        Files.createDirectories(paths.root.resolve("data"))
    }

    fun ensureYtDlp(url: String? = null): Path {
        initializeDirectories()
        val target = paths.ytDlp.resolve("yt-dlp.exe")
        if (Files.isRegularFile(target)) return target

        require(!url.isNullOrBlank()) {
            "yt-dlp.exe is missing. Supply a download URL or install yt-dlp manually."
        }

        download(url, target)
        return target
    }

    fun installZip(toolDir: Path, zipUrl: String): Path {
        initializeDirectories()

        val zip = Files.createTempFile(paths.cache, "runtime-", ".zip")
        download(zipUrl, zip)
        Files.createDirectories(toolDir)

        ZipInputStream(Files.newInputStream(zip)).use { input ->
            while (true) {
                val entry = input.nextEntry ?: break
                if (entry.name.startsWith("/") || entry.name.contains("..")) continue

                val destination = toolDir.resolve(entry.name).normalize()
                if (!destination.startsWith(toolDir)) continue

                if (entry.isDirectory) {
                    Files.createDirectories(destination)
                } else {
                    destination.parent?.let(Files::createDirectories)
                    Files.newOutputStream(destination).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }

        Files.deleteIfExists(zip)
        return toolDir
    }

    private fun download(url: String, target: Path) {
        val request = HttpRequest.newBuilder(URI(url)).GET().build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())

        check(response.statusCode() in 200..299) {
            "Runtime download failed: HTTP " + response.statusCode()
        }

        target.parent?.let(Files::createDirectories)
        Files.write(target, response.body())
    }
}
