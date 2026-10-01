package com.deniscerri.ytdl.windows.model

data class CookieProfile(
    val id: String,
    val name: String,
    val source: String = "",
    val cookieFile: String? = null,
    val browser: String? = null
)
