package com.campus.lostfound.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.campus.lostfound.CampusApp
import com.campus.lostfound.Session
import com.campus.lostfound.data.CreateClaimRequest
import com.campus.lostfound.data.ItemDetailDto
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(itemId: String, onBack: () -> Unit) {
    val app = CampusApp.instance
    val token = Session.token
    var detail by remember { mutableStateOf<ItemDetailDto?>(null) }
    var claimMessage by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            try {
                val res = app.api.item(itemId)
                if (!res.isSuccessful) error = res.errorBody()?.string() else {
                    detail = res.body()
                    error = null
                }
            } catch (e: Exception) {
                error = e.message
            }
        }
    }

    LaunchedEffect(itemId) { load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.title ?: "Item") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { pad ->
        val d = detail
        if (d == null) {
            Text(
                error ?: "Loading…",
                modifier = Modifier.padding(pad).padding(16.dp),
                color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
            return@Scaffold
        }
        Column(
            Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            d.imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Item image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            Text("${d.kind} · ${d.status}", style = MaterialTheme.typography.labelLarge)
            Text(d.description, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
            Text("Where: ${d.location}", modifier = Modifier.padding(top = 8.dp))
            Text("Category: ${d.category}", modifier = Modifier.padding(top = 4.dp))
            d.owner?.let { Text("Posted by: ${it.displayName}", modifier = Modifier.padding(top = 8.dp)) }

            if (token != null && d.status == "OPEN") {
                OutlinedTextField(
                    claimMessage,
                    { claimMessage = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    label = { Text("Why is this yours?") },
                    minLines = 2,
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
                Button(
                    onClick = {
                        scope.launch {
                            busy = true
                            error = null
                            try {
                                val res = app.api.claim(
                                    "Bearer $token",
                                    CreateClaimRequest(d.id, claimMessage.ifBlank { "I believe this is my item." }),
                                )
                                if (!res.isSuccessful) error = res.errorBody()?.string() else load()
                            } catch (e: Exception) {
                                error = e.message
                            } finally {
                                busy = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    enabled = !busy && token != null,
                ) {
                    Text("Submit claim")
                }
            }

            Text("Claims", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 24.dp))
            val claims = d.claims.orEmpty()
            if (claims.isEmpty()) Text("No claims yet.", modifier = Modifier.padding(top = 8.dp))
            claims.forEach { c ->
                Text(
                    "· ${c.user?.displayName ?: "?"}: ${c.message} (${c.status})",
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}
