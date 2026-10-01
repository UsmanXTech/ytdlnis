package com.deniscerri.ytdl.windows.runtime

import com.deniscerri.ytdl.windows.engine.YtdlRequest

object WindowsCommonOptions {
    fun addDefaults(request: YtdlRequest): YtdlRequest {
        request.addOption("--newline")
        request.addOption("--no-playlist")
        return request
    }
}
