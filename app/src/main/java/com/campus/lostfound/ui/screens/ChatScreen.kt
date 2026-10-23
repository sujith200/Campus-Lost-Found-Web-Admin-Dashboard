package com.campus.lostfound.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.campus.lostfound.CampusApp
import com.campus.lostfound.Session
import com.campus.lostfound.data.ChatRequest
import kotlinx.coroutines.launch

data class ChatLine(val who: String, val text: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(onBack: () -> Unit) {
    val app = CampusApp.instance
    val token = Session.token
    val lines = remember { mutableStateListOf<ChatLine>() }
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    if (lines.isEmpty()) {
        lines.add(ChatLine("bot", "Hi! Ask about posting items, claims, desk hours, or campus security."))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI support") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
                items(lines) { line ->
                    val style = if (line.who == "user") MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium
                    Text(
                        (if (line.who == "user") "You: " else "Assistant: ") + line.text,
                        style = style,
                        modifier = Modifier.padding(vertical = 6.dp),
                    )
                }
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp)) }
            OutlinedTextField(
                input,
                { input = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                label = { Text("Message") },
                minLines = 2,
            )
            Button(
                onClick = {
                    val t = token ?: run {
                        error = "Sign in to use chat."
                        return@Button
                    }
                    val msg = input.trim()
                    if (msg.isEmpty()) return@Button
                    lines.add(ChatLine("user", msg))
                    input = ""
                    scope.launch {
                        busy = true
                        error = null
                        try {
                            val res = app.api.chat("Bearer $t", ChatRequest(msg))
                            if (!res.isSuccessful) error = res.errorBody()?.string()
                            else lines.add(ChatLine("bot", res.body()?.reply ?: "(no reply)"))
                        } catch (e: Exception) {
                            error = e.message
                        } finally {
                            busy = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                enabled = !busy && token != null,
            ) {
                Text(if (busy) "…" else "Send")
            }
        }
    }
}
