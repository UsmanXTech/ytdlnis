package com.deniscerri.ytdl.windows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deniscerri.ytdl.windows.engine.ProgressUpdate
import com.deniscerri.ytdl.windows.engine.YtdlRequest
import com.deniscerri.ytdl.windows.runtime.WindowsYtdlEngine
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(engine: WindowsYtdlEngine) {
    MaterialTheme {
        val scope = rememberCoroutineScope()
        var url by remember { mutableStateOf("") }
        var output by remember { mutableStateOf("") }
        var progress by remember { mutableStateOf(-1f) }
        var status by remember { mutableStateOf("Ready") }

        Scaffold(
            topBar = { TopAppBar(title = { Text("YTDLnis") }) }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Download", style = MaterialTheme.typography.headlineSmall)

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("URL") },
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (url.isBlank()) {
                            status = "Enter a URL"
                            return@Button
                        }
                        scope.launch {
                            status = "Downloading…"
                            progress = -1f
                            val lines = mutableListOf<String>()
                            try {
                                val result = engine.execute(
                                    YtdlRequest(url),
                                    processId = "desktop-download",
                                    onOutput = { update: ProgressUpdate ->
                                        if (update.progress >= 0f) progress = update.progress / 100f
                                        lines.add(update.line)
                                    }
                                )
                                output = lines.takeLast(80).joinToString("\n")
                                status = if (result.exitCode == 0) "Completed" else "Failed"
                                if (result.exitCode == 0) progress = 1f
                            } catch (e: Exception) {
                                output = e.message.orEmpty()
                                status = "Failed"
                            }
                        }
                    }
                ) {
                    Text("Download")
                }

                if (progress >= 0f) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(status, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(output.ifBlank { "Download output will appear here." })
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { engine.cancel("desktop-download") }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
