package com.deniscerri.ytdl.windows.engine

class YtdlRequest(private val urls: List<String>) {
    private val options = linkedMapOf<String, MutableList<String>>()
    private val customCommands = mutableListOf<String>()

    constructor(url: String) : this(listOf(url))

    fun addOption(option: String, argument: String): YtdlRequest {
        options.getOrPut(option) { mutableListOf() }.add(argument)
        return this
    }

    fun addOption(option: String): YtdlRequest {
        options.getOrPut(option) { mutableListOf() }.add("")
        return this
    }

    fun addCommand(command: String): YtdlRequest {
        customCommands += command
        return this
    }

    fun buildCommand(): List<String> = buildList {
        options.forEach { (option, args) ->
            args.forEach { arg ->
                add(option)
                if (arg.isNotEmpty()) add(arg)
            }
        }
        addAll(customCommands)
        addAll(urls)
    }
}
