package com.deniscerri.ytdl.windows.model

data class DownloadRequest(
    val url: String,
    val outputDirectory: String = "",
    val audioOnly: Boolean = false,
    val format: String? = null,
    val outputTemplate: String? = null,
    val embedMetadata: Boolean = true,
    val embedThumbnail: Boolean = false
)
