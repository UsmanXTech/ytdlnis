package com.deniscerri.ytdl.windows

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deniscerri.ytdl.windows.data.CookieStore
import com.deniscerri.ytdl.windows.data.DownloadStore
import com.deniscerri.ytdl.windows.data.SettingsStore
import com.deniscerri.ytdl.windows.data.TemplateStore
import com.deniscerri.ytdl.windows.model.*
import com.deniscerri.ytdl.windows.runtime.DownloadCommandBuilder
import com.deniscerri.ytdl.windows.runtime.WindowsRuntimeBootstrap
import com.deniscerri.ytdl.windows.runtime.WindowsYtdlEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

private enum class Screen { DOWNLOAD, HISTORY, TEMPLATES, SETTINGS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(engine: WindowsYtdlEngine) {
    val scope = rememberCoroutineScope()
    val downloadStore = remember { DownloadStore() }
    val templateStore = remember { TemplateStore() }
    val cookieStore = remember { CookieStore() }
    val settingsStore = remember { SettingsStore() }
    val bootstrap = remember { WindowsRuntimeBootstrap() }

    var screen by remember { mutableStateOf(Screen.DOWNLOAD) }
    var url by remember { mutableStateOf("") }
    var outputDir by remember { mutableStateOf(settingsStore.load().defaultOutputDirectory) }
    var format by remember { mutableStateOf(settingsStore.load().defaultFormat) }
    var audioOnly by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Ready") }
    var downloads by remember { mutableStateOf(downloadStore.all()) }
    var templates by remember { mutableStateOf(templateStore.all()) }
    var cookies by remember { mutableStateOf(cookieStore.all()) }
    var settings by remember { mutableStateOf(settingsStore.load()) }
    var templateTitle by remember { mutableStateOf("") }
    var templateCommand by remember { mutableStateOf("") }

    fun refresh() {
        downloads = downloadStore.all()
        templates = templateStore.all()
        cookies = cookieStore.all()
        settings = settingsStore.load()
    }

    MaterialTheme {
        NavigationRailScaffold(
            screen = screen,
            onScreenChange = { screen = it }
        ) {
            when (screen) {
                Screen.DOWNLOAD -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Download", style = MaterialTheme.typography.headlineSmall)
                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("URL") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = outputDir,
                            onValueChange = { outputDir = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Output directory") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = format,
                            onValueChange = { format = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Format") },
                            singleLine = true,
                            placeholder = { Text("bestvideo+bestaudio/best") }
                        )
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Checkbox(audioOnly, { audioOnly = it })
                            Text("Audio only")
                        }

                        Button(
                            onClick = {
                                val cleanUrl = url.trim()
                                if (cleanUrl.isBlank()) {
                                    status = "Enter a URL."
                                    return@Button
                                }
                                val id = UUID.randomUUID().toString()
                                val request = DownloadRequest(
                                    url = cleanUrl,
                                    outputDirectory = outputDir.trim(),
                                    audioOnly = audioOnly,
                                    format = format.trim().takeIf(String::isNotBlank),
                                    embedMetadata = settings.embedMetadata,
                                    embedThumbnail = settings.embedThumbnail
                                )
                                val initial = DownloadEntry(
                                    id = id,
                                    url = cleanUrl,
                                    outputDir = request.outputDirectory
                                )
                                downloadStore.upsert(initial)
                                refresh()
                                screen = Screen.HISTORY

                                scope.launch(Dispatchers.IO) {
                                    try {
                                        bootstrap.initializeDirectories()
                                        downloadStore.upsert(initial.copy(status = DownloadStatus.ACTIVE))
                                        val result = engine.execute(
                                            DownloadCommandBuilder.build(request),
                                            processId = id,
                                            onOutput = { update ->
                                                val current = downloadStore.all().firstOrNull { it.id == id } ?: initial
                                                downloadStore.upsert(
                                                    current.copy(
                                                        progress = update.progress,
                                                        etaSeconds = update.etaSeconds,
                                                        message = update.line.takeLast(500)
                                                    )
                                                )
                                            }
                                        )
                                        val current = downloadStore.all().firstOrNull { it.id == id } ?: initial
                                        downloadStore.upsert(
                                            current.copy(
                                                status = if (result.exitCode == 0) DownloadStatus.COMPLETED else DownloadStatus.FAILED,
                                                progress = if (result.exitCode == 0) 100f else current.progress,
                                                message = result.stdout.takeLast(500),
                                                completedAt = System.currentTimeMillis()
                                            )
                                        )
                                    } catch (t: Throwable) {
                                        val current = downloadStore.all().firstOrNull { it.id == id } ?: initial
                                        downloadStore.upsert(
                                            current.copy(
                                                status = if (t is java.util.concurrent.CancellationException) DownloadStatus.CANCELLED else DownloadStatus.FAILED,
                                                message = t.message.orEmpty()
                                            )
                                        )
                                    }
                                    withContext(Dispatchers.Main) {
                                        status = "Download finished."
                                        refresh()
                                    }
                                }
                            }
                        ) {
                            Text("Download")
                        }
                        if (status != "Ready") Text(status)
                    }
                }

                Screen.HISTORY -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(onClick = ::refresh) { Text("Refresh") }
                                Button(onClick = {
                                    downloads.filter { it.status == DownloadStatus.ACTIVE }
                                        .forEach { engine.cancel(it.id) }
                                    refresh()
                                }) { Text("Cancel active") }
                            }
                        }
                        items(downloads, key = { it.id }) { item ->
                            ElevatedCard(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(item.url, style = MaterialTheme.typography.titleMedium)
                                    Text(item.status.name)
                                    if (item.progress >= 0) {
                                        LinearProgressIndicator(
                                            progress = { item.progress / 100f },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    if (item.etaSeconds >= 0) Text("ETA: " + item.etaSeconds + "s")
                                    if (item.message.isNotBlank()) Text(item.message)
                                }
                            }
                        }
                    }
                }

                Screen.TEMPLATES -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Command Templates", style = MaterialTheme.typography.headlineSmall)
                        OutlinedTextField(
                            templateTitle, { templateTitle = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Template name") }
                        )
                        OutlinedTextField(
                            templateCommand, { templateCommand = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("yt-dlp options") },
                            placeholder = { Text("--format bestvideo+bestaudio/best --extract-audio") }
                        )
                        Button(onClick = {
                            if (templateTitle.isBlank() || templateCommand.isBlank()) return@Button
                            templateStore.upsert(
                                TemplateEntry(
                                    id = UUID.randomUUID().toString(),
                                    title = templateTitle.trim(),
                                    command = templateCommand.trim()
                                )
                            )
                            templateTitle = ""
                            templateCommand = ""
                            refresh()
                        }) { Text("Save template") }

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(templates, key = { it.id }) { template ->
                                ElevatedCard(Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(template.title, style = MaterialTheme.typography.titleMedium)
                                        Text(template.command)
                                        Button(onClick = {
                                            url = url.trim()
                                            val request = com.deniscerri.ytdl.windows.runtime.TemplateCommandParser
                                                .toRequest(template, url)
                                            audioOnly = request.audioOnly
                                            format = request.format.orEmpty()
                                            screen = Screen.DOWNLOAD
                                        }) { Text("Use") }
                                    }
                                }
                            }
                        }
                    }
                }

                Screen.SETTINGS -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Settings", style = MaterialTheme.typography.headlineSmall)

                        OutlinedTextField(
                            settings.defaultOutputDirectory,
                            {
                                settings = settings.copy(defaultOutputDirectory = it)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Default output directory") }
                        )
                        OutlinedTextField(
                            settings.defaultFormat,
                            {
                                settings = settings.copy(defaultFormat = it)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Default format") }
                        )
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Checkbox(settings.embedMetadata, {
                                settings = settings.copy(embedMetadata = it)
                            })
                            Text("Embed metadata")
                        }
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Checkbox(settings.embedThumbnail, {
                                settings = settings.copy(embedThumbnail = it)
                            })
                            Text("Embed thumbnail")
                        }
                        OutlinedTextField(
                            settings.browser,
                            { settings = settings.copy(browser = it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Browser for cookies") },
                            placeholder = { Text("chrome / edge / firefox") }
                        )
                        Button(onClick = {
                            settingsStore.save(settings)
                            outputDir = settings.defaultOutputDirectory
                            format = settings.defaultFormat
                            status = "Settings saved."
                        }) { Text("Save settings") }

                        HorizontalDivider()
                        Text("Cookie profiles", style = MaterialTheme.typography.titleMedium)
                        Button(onClick = {
                            val id = UUID.randomUUID().toString()
                            cookieStore.upsert(
                                CookieProfile(
                                    id = id,
                                    name = "Browser cookies",
                                    source = "browser",
                                    browser = settings.browser.takeIf(String::isNotBlank)
                                )
                            )
                            refresh()
                        }) { Text("Save browser cookie profile") }

                        cookies.forEach { cookie ->
                            ListItem(
                                headlineContent = { Text(cookie.name) },
                                supportingContent = {
                                    Text(cookie.browser?.let { "Browser: " + it } ?: cookie.source)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationRailScaffold(
    screen: Screen,
    onScreenChange: (Screen) -> Unit,
    content: @Composable () -> Unit
) {
    Row(Modifier.fillMaxSize()) {
        NavigationRail {
            NavigationRailItem(
                selected = screen == Screen.DOWNLOAD,
                onClick = { onScreenChange(Screen.DOWNLOAD) },
                icon = { Icon(Icons.Outlined.Download, null) },
                label = { Text("Download") }
            )
            NavigationRailItem(
                selected = screen == Screen.HISTORY,
                onClick = { onScreenChange(Screen.HISTORY) },
                icon = { Icon(Icons.Outlined.History, null) },
                label = { Text("History") }
            )
            NavigationRailItem(
                selected = screen == Screen.TEMPLATES,
                onClick = { onScreenChange(Screen.TEMPLATES) },
                icon = { Icon(Icons.Outlined.Terminal, null) },
                label = { Text("Templates") }
            )
            NavigationRailItem(
                selected = screen == Screen.SETTINGS,
                onClick = { onScreenChange(Screen.SETTINGS) },
                icon = { Icon(Icons.Outlined.Settings, null) },
                label = { Text("Settings") }
            )
        }
        Box(Modifier.fillMaxSize()) {
            content()
        }
    }
}
