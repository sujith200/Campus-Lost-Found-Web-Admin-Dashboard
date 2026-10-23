package com.campus.lostfound.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.campus.lostfound.CampusApp
import com.campus.lostfound.Session
import com.campus.lostfound.data.CreateItemRequest
import com.campus.lostfound.data.ItemDto
import com.campus.lostfound.data.MatchRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(onBack: () -> Unit, onPosted: () -> Unit) {
    val app = CampusApp.instance
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var location by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf("LOST") }
    var matches by remember { mutableStateOf<List<ItemDto>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val scope = rememberCoroutineScope()
    val token = Session.token ?: return
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New listing") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
            )
        },
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            RowChips(kind) { kind = it }
            OutlinedTextField(title, { title = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Title") })
            OutlinedTextField(description, { description = it }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), label = { Text("Description") }, minLines = 3)
            OutlinedTextField(category, { category = it }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), label = { Text("Category") })
            OutlinedTextField(location, { location = it }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), label = { Text("Location") })
            Button(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                Text("Upload image")
            }
            Text(
                text = selectedImageUri?.toString() ?: "No image selected",
                modifier = Modifier.padding(top = 6.dp),
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Tip: clear image photos help others identify your item quickly.",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
            error?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }

            Button(
                onClick = {
                    scope.launch {
                        busy = true
                        error = null
                        try {
                            val res = app.api.match(MatchRequest(title, description, category, location, kind))
                            if (res.isSuccessful) matches = res.body()?.results.orEmpty() else error = res.errorBody()?.string()
                        } catch (e: Exception) {
                            error = e.message
                        } finally {
                            busy = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                enabled = !busy && title.isNotBlank() && description.isNotBlank(),
            ) {
                Text("Run smart match")
            }
            if (matches.isNotEmpty()) {
                Text("Suggested matches (${matches.size})", modifier = Modifier.padding(top = 16.dp))
                matches.take(5).forEach { m ->
                    Text("· ${m.title} (${m.matchScore ?: 0}%)", modifier = Modifier.padding(top = 4.dp))
                }
            }
            Button(
                onClick = {
                    scope.launch {
                        busy = true
                        error = null
                        try {
                            val encodedImage = selectedImageUri?.let { uri ->
                                toDataUrl(context, uri)
                            }
                            val res = app.api.createItem(
                                "Bearer $token",
                                CreateItemRequest(
                                    title = title,
                                    description = description,
                                    category = category,
                                    location = location,
                                    kind = kind,
                                    imageUrl = encodedImage,
                                ),
                            )
                            if (!res.isSuccessful) error = res.errorBody()?.string() else onPosted()
                        } catch (e: Exception) {
                            error = e.message
                        } finally {
                            busy = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                enabled = !busy && title.isNotBlank(),
            ) {
                Text("Publish listing")
            }
        }
    }
}

private fun toDataUrl(context: Context, uri: Uri): String? {
    return runCatching {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
        "data:image/*;base64,$encoded"
    }.getOrNull()
}

@Composable
private fun RowChips(kind: String, onKind: (String) -> Unit) {
    Row(Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = kind == "LOST", onClick = { onKind("LOST") }, label = { Text("Lost") })
        FilterChip(selected = kind == "FOUND", onClick = { onKind("FOUND") }, label = { Text("Found") })
    }
}
