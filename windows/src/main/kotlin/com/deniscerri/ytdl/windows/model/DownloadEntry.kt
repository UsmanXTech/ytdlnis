package com.deniscerri.ytdl.windows.model

import java.time.Instant

enum class DownloadStatus { QUEUED, ACTIVE, COMPLETED, FAILED, CANCELLED }

data class DownloadEntry(
    val id: String,
    val url: String,
    val title: String = "",
    val outputDir: String = "",
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val progress: Float = -1f,
    val etaSeconds: Long = -1L,
    val message: String = "",
    val createdAt: Long = Instant.now().toEpochMilli(),
    val completedAt: Long? = null
)
