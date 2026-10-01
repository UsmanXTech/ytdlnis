package com.deniscerri.ytdl.windows

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deniscerri.ytdl.windows.data.DownloadStore
import com.deniscerri.ytdl.windows.model.DownloadEntry
import com.deniscerri.ytdl.windows.model.DownloadRequest
import com.deniscerri.ytdl.windows.model.DownloadStatus
import com.deniscerri.ytdl.windows.runtime.DownloadCommandBuilder
import com.deniscerri.ytdl.windows.runtime.WindowsRuntimeBootstrap
import com.deniscerri.ytdl.windows.runtime.WindowsYtdlEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(engine: WindowsYtdlEngine) {
    val store = remember { DownloadStore() }
    val bootstrap = remember { WindowsRuntimeBootstrap() }
    val scope = rememberCoroutineScope()

    var url by remember { mutableStateOf("") }
    var outputDir by remember { mutableStateOf("") }
    var audioOnly by remember { mutableStateOf(false) }
    var format by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var statusMessage by remember { mutableStateOf("Ready") }
    var downloads by remember { mutableStateOf(store.all()) }

    fun refresh() {
        downloads = store.all()
    }

    MaterialTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text("YTDLnis") }) }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selectedTab == 0, { selectedTab = 0 }, text = { Text("Download") })
                    Tab(selectedTab == 1, { selectedTab = 1 }, text = { Text("Queue / History") })
                }

                if (statusMessage != "Ready") {
                    Text(statusMessage, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp))
                }

                when (selectedTab) {
                    0 -> Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                            label = { Text("Format selector") },
                            singleLine = true,
                            placeholder = { Text("bestvideo+bestaudio/best") }
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Checkbox(audioOnly, { audioOnly = it })
                            Text("Audio only", modifier = Modifier.padding(top = 12.dp))
                        }

                        Button(
                            onClick = {
                                val cleanUrl = url.trim()
                                if (cleanUrl.isBlank()) {
                                    statusMessage = "Enter a URL."
                                    return@Button
                                }

                                scope.launch(Dispatchers.IO) {
                                    try {
                                        bootstrap.initializeDirectories()
                                        val id = UUID.randomUUID().toString()
                                        val request = DownloadRequest(
                                            url = cleanUrl,
                                            outputDirectory = outputDir.trim(),
                                            audioOnly = audioOnly,
                                            format = format.trim().takeIf(String::isNotBlank)
                                        )
                                        val initial = DownloadEntry(
                                            id = id,
                                            url = cleanUrl,
                                            outputDir = request.outputDirectory,
                                            status = DownloadStatus.QUEUED
                                        )
                                        store.upsert(initial)

                                        store.upsert(initial.copy(status = DownloadStatus.ACTIVE))
                                        val result = engine.execute(
                                            DownloadCommandBuilder.build(request),
                                            processId = id,
                                            onOutput = { update ->
                                                val current = store.all().firstOrNull { it.id == id } ?: initial
                                                store.upsert(
                                                    current.copy(
                                                        progress = update.progress,
                                                        etaSeconds = update.etaSeconds,
                                                        message = update.line.takeLast(500)
                                                    )
                                                )
                                            }
                                        )
                                        val current = store.all().firstOrNull { it.id == id } ?: initial
                                        store.upsert(
                                            current.copy(
                                                status = if (result.exitCode == 0) DownloadStatus.COMPLETED else DownloadStatus.FAILED,
                                                progress = if (result.exitCode == 0) 100f else current.progress,
                                                message = result.stdout.takeLast(500)
                                            )
                                        )
                                    } catch (t: Throwable) {
                                        val failed = store.all().firstOrNull { it.url == cleanUrl }
                                        if (failed != null) store.upsert(failed.copy(status = DownloadStatus.FAILED, message = t.message.orEmpty()))
                                    }

                                    withContext(Dispatchers.Main) {
                                        statusMessage = "Download finished."
                                        refresh()
                                        selectedTab = 1
                                    }
                                }
                            }
                        ) {
                            Text("Download")
                        }
                    }

                    1 -> LazyColumn(
                        Modifier.fillMaxSize().padding(24.dp),
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
                                    if (item.progress >= 0f) {
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
            }
        }
    }
}
