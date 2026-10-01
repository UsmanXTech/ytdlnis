package com.deniscerri.ytdl.windows.runtime

import com.deniscerri.ytdl.windows.engine.ProgressUpdate
import com.deniscerri.ytdl.windows.engine.YtdlRequest
import com.deniscerri.ytdl.windows.model.DownloadRequest
import java.io.File

object DownloadCommandBuilder {
    fun build(request: DownloadRequest): YtdlRequest {
        val ytdl = YtdlRequest(request.url)

        if (request.audioOnly) {
            ytdl.addOption("--extract-audio")
            ytdl.addOption("--audio-format", "mp3")
        }

        request.format?.takeIf { it.isNotBlank() }?.let {
            ytdl.addOption("--format", it)
        }

        request.outputTemplate?.takeIf { it.isNotBlank() }?.let {
            ytdl.addOption("--output", it)
        }

        request.outputDirectory.takeIf { it.isNotBlank() }?.let {
            val template = File(it, "%(title)s.%(ext)s").path
            ytdl.addOption("--output", template)
        }

        if (request.embedMetadata) ytdl.addOption("--embed-metadata")
        if (request.embedThumbnail) ytdl.addOption("--embed-thumbnail")

        return ytdl
    }
}
