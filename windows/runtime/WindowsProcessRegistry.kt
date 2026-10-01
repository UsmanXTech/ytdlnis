package com.deniscerri.ytdl.windows.runtime

import java.util.concurrent.ConcurrentHashMap

class WindowsProcessRegistry {
    private val processes = ConcurrentHashMap<String, Process>()

    fun register(id: String, process: Process) {
        check(processes.putIfAbsent(id, process) == null) {
            "Process ID already exists: $id"
        }
    }

    fun remove(id: String) {
        processes.remove(id)
    }

    fun cancel(id: String): Boolean {
        val process = processes.remove(id) ?: return false
        if (!process.isAlive) return false
        process.destroy()
        if (process.isAlive) process.destroyForcibly()
        return true
    }

    fun isRunning(id: String): Boolean = processes[id]?.isAlive == true
}
