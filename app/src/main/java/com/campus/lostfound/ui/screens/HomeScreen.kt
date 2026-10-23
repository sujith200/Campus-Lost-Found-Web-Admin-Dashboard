package com.campus.lostfound.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campus.lostfound.CampusApp
import com.campus.lostfound.Session
import com.campus.lostfound.data.ItemDto
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPost: () -> Unit,
    onChat: () -> Unit,
    onProfile: () -> Unit,
    onItem: (String) -> Unit,
    onLogout: () -> Unit,
) {
    val app = CampusApp.instance
    var items by remember { mutableStateOf<List<ItemDto>>(emptyList()) }
    var filter by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("DASHBOARD") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            try {
                val res = app.api.items(kind = filter, q = query.ifBlank { null })
                if (!res.isSuccessful) {
                    error = res.errorBody()?.string() ?: "Load failed"
                    return@launch
                }
                items = res.body().orEmpty()
                error = null
            } catch (e: Exception) {
                error = e.message
            }
        }
    }

    LaunchedEffect(filter, query) { load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Student Home",
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Start,
                    )
                },
                actions = {
                    TextButton(onClick = onChat) { Text("Support", fontSize = 12.sp) }
                    TextButton(onClick = onPost) { Text("Post", fontSize = 12.sp) }
                    TextButton(onClick = onProfile) { Text("Profile", fontSize = 12.sp) }
                    TextButton(onClick = { Session.save(app, null); onLogout() }) { Text("Out", fontSize = 12.sp) }
                },
            )
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(selected = section == "DASHBOARD", onClick = { section = "DASHBOARD" }, label = { Text("Dashboard") })
                FilterChip(selected = section == "BUFFS", onClick = { section = "BUFFS" }, label = { Text("Study Buffs") })
            }

            if (section == "BUFFS") {
                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    item {
                        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Study Buff #1", style = MaterialTheme.typography.titleMedium)
                                Text("Use 50/10 focus cycles: 50 min deep study, 10 min break.")
                            }
                        }
                    }
                    item {
                        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Study Buff #2", style = MaterialTheme.typography.titleMedium)
                                Text("Keep essentials labeled with your name and contact to reduce loss risk.")
                            }
                        }
                    }
                    item {
                        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Study Buff #3", style = MaterialTheme.typography.titleMedium)
                                Text("Before leaving class or library, do a 20-second desk scan checklist.")
                            }
                        }
                    }
                }
                return@Scaffold
            }

            Column(Modifier.padding(horizontal = 16.dp)) {
                StatCard("Total", items.size.toString(), Modifier.fillMaxWidth())
                StatCard("Lost", items.count { it.kind == "LOST" }.toString(), Modifier.fillMaxWidth())
                StatCard("Found", items.count { it.kind == "FOUND" }.toString(), Modifier.fillMaxWidth())
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("All") })
                FilterChip(selected = filter == "LOST", onClick = { filter = "LOST" }, label = { Text("Lost") })
                FilterChip(selected = filter == "FOUND", onClick = { filter = "FOUND" }, label = { Text("Found") })
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                label = { Text("Search") },
                singleLine = true,
            )
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp)) }
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items(items, key = { it.id }) { item ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { onItem(item.id) },
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            item.imageUrl?.let { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Item image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(170.dp)
                                        .padding(bottom = 8.dp),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                            Text("${item.kind} · ${item.status}", style = MaterialTheme.typography.bodySmall)
                            Text(item.location, style = MaterialTheme.typography.bodyMedium)
                            AssistChip(onClick = {}, label = { Text(item.category) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier.padding(vertical = 8.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}
