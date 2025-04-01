package com.ekpss.quizapp.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ekpss.quizapp.firebase.FirestoreHelper
import com.ekpss.quizapp.model.Question

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedQuestionsScreen(subject: String, onBack: () -> Unit)
{
    val firestoreHelper = remember { FirestoreHelper() }
    var savedQuestions by remember { mutableStateOf<List<Question>>(emptyList()) }

    LaunchedEffect(true) {
        firestoreHelper.getBookmarkedQuestionsBySubject(
            subject = subject,
            onSuccess = { savedQuestions = it },
            onFailure = { Log.e("SavedQuestions", "Hata: ${it.message}") }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kaydedilen Sorular") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(savedQuestions.size) { index ->
                val question = savedQuestions[index]

                Text("${index + 1}. ${question.question}", fontWeight = FontWeight.Bold)
                Text("Cevap: ${question.answer}", color = Color(0xFF388E3C))
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        firestoreHelper.deleteBookmarkedQuestion(
                            subject = subject,
                            question = question,
                            onSuccess = {
                                savedQuestions = savedQuestions.filterNot { it == question }
                            },
                            onFailure = { Log.e("Delete", "Soru silinemedi: ${it.message}") }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Sil", color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

