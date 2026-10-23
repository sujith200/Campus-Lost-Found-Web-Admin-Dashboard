package com.campus.lostfound.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.campus.lostfound.CampusApp
import com.campus.lostfound.Session
import com.campus.lostfound.data.LoginRequest
import com.campus.lostfound.data.RegisterRequest
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoggedIn: () -> Unit) {
    var email by remember { mutableStateOf("student@campus.edu") }
    var password by remember { mutableStateOf("student123") }
    var displayName by remember { mutableStateOf("New Student") }
    var registerMode by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val app = CampusApp.instance

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(if (registerMode) "Create account" else "Campus Lost & Found")
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            label = { Text("Email") },
            singleLine = true,
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
        )
        if (registerMode) {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                label = { Text("Display name") },
                singleLine = true,
            )
        }
        error?.let {
            Text(it, modifier = Modifier.padding(top = 8.dp), color = androidx.compose.material3.MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = {
                scope.launch {
                    busy = true
                    error = null
                    try {
                        val res = if (registerMode) {
                            app.api.register(RegisterRequest(email, password, displayName))
                        } else {
                            app.api.login(LoginRequest(email, password))
                        }
                        if (!res.isSuccessful) {
                            error = res.errorBody()?.string() ?: "Request failed"
                            return@launch
                        }
                        val body = res.body() ?: run {
                            error = "Empty response"
                            return@launch
                        }
                        Session.save(app, body.token)
                        onLoggedIn()
                    } catch (e: Exception) {
                        error = e.message ?: "Network error"
                    } finally {
                        busy = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            enabled = !busy,
        ) {
            Text(if (busy) "…" else if (registerMode) "Register" else "Sign in")
        }
        TextButton(onClick = { registerMode = !registerMode }) {
            Text(if (registerMode) "Have an account? Sign in" else "New user? Register")
        }
    }
}
