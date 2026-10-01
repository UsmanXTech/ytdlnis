package com.deniscerri.ytdl.windows.runtime

import com.deniscerri.ytdl.windows.model.DownloadRequest
import com.deniscerri.ytdl.windows.model.TemplateEntry
import java.util.UUID

object TemplateCommandParser {
    fun toRequest(template: TemplateEntry, url: String): DownloadRequest {
        val tokens = template.command.trim().split(Regex("\\s+")).filter(String::isNotBlank)
        var outputTemplate: String? = null
        var format: String? = null
        var audioOnly = false

        var index = 0
        while (index < tokens.size) {
            when (tokens[index]) {
                "--format", "-f" -> if (index + 1 < tokens.size) {
                    format = tokens[++index]
                }
                "--extract-audio" -> audioOnly = true
                "--output", "-o" -> if (index + 1 < tokens.size) {
                    outputTemplate = tokens[++index]
                }
            }
            index++
        }

        return DownloadRequest(
            url = url,
            audioOnly = audioOnly,
            format = format,
            outputTemplate = outputTemplate
        )
    }

    fun newId(): String = UUID.randomUUID().toString()
}
