package com.ekpss.quizapp.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ekpss.quizapp.firebase.FirestoreHelper
import com.ekpss.quizapp.model.Note

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(subject: String, onBack: () -> Unit) {
    val firestoreHelper = remember { FirestoreHelper() }
    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Notları çek
    LaunchedEffect(subject) {
        Log.d("NoteScreen", "LaunchedEffect çalıştı: $subject")
        firestoreHelper.getNotes(
            subject,

            onSuccess = {
                notes = it
                isLoading = false
            },
            onFailure = {
                Log.e("NoteScreen", "Notlar alınamadı: ${it.message}")
                isLoading = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$subject - Notlar") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Reklam Alanı (Banner)", style = MaterialTheme.typography.labelSmall)
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(notes.size) { index ->
                        val note = notes[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(note.title, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(note.content, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
