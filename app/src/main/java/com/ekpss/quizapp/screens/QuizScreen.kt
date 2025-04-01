package com.ekpss.quizapp.screens

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.ekpss.quizapp.firebase.FirestoreHelper
import com.ekpss.quizapp.model.Question
import kotlin.math.roundToInt
import androidx.compose.ui.res.painterResource
import com.ekpss.quizapp.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun QuizScreen(
    subject: String,
    testId: String,
    onBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateSavedQuestions: () -> Unit
) {
    val firestoreHelper = remember { FirestoreHelper() }
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    var showResult by remember { mutableStateOf(false) }
    val answers = remember { mutableStateMapOf<Int, Pair<String?, Boolean>>() }
    val savedQuestions = remember { mutableStateListOf<Question>() }
    val bookmarkedStates = remember { mutableStateMapOf<Int, Boolean>() }
    val bookmarkedQuestions = remember { mutableStateListOf<Question>() }


    LaunchedEffect(subject) {
        firestoreHelper.getQuestions(
            subject = subject,
            testId = testId,
            onSuccess = { fetchedQuestions ->
                questions = fetchedQuestions

                // Kullanıcının kaydettiği tüm soruları al
                firestoreHelper.getBookmarkedQuestions(
                    userId = "default_user",
                    onSuccess = { bookmarkedList ->
                        fetchedQuestions.forEachIndexed { index, question ->
                            val isBookmarked = bookmarkedList.any {
                                it.question == question.question && it.testId == testId
                            }
                            bookmarkedStates[index] = isBookmarked
                        }
                    },
                    onFailure = {
                        Log.e("QuizScreen", "Bookmark kontrolü başarısız: ${it.message}")
                    }
                )
            },
            onFailure = {
                Log.e("QuizScreen", "Soru çekilemedi: ${it.message}")
            }
        )
    }

    fun saveQuestionToFirestore(subject: String, question: Question) {
        firestoreHelper.saveBookmarkedQuestion(
            subject = subject,
            question = question.copy(testId = testId),
            onSuccess = { Log.d("QuizScreen", "Soru kaydedildi: ${question.question}") },
            onFailure = { Log.e("QuizScreen", "Soru kaydedilemedi: ${it.message}") }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$subject - Test") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (questions.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (showResult) {
                val score = answers.count { (index, pair) -> questions[index].answer == pair.first }
                val percentage = (score.toFloat() / questions.size * 100).roundToInt()
                val resultColor = when {
                    percentage >= 80 -> Color(0xFF81C784)
                    percentage >= 50 -> Color(0xFFFFF176)
                    else -> Color(0xFFE57373)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(resultColor.copy(alpha = 0.1f))
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Test Bitti!", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Puanın: $score / ${questions.size} ($percentage%)", fontSize = 16.sp)

                    LinearProgressIndicator(
                        progress = percentage / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = resultColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            currentIndex = 0
                            answers.clear()
                            savedQuestions.clear()
                            showResult = false
                        }) {
                            Text("Yeniden Dene")
                        }
                        Button(onClick = onNavigateHome) {
                            Text("Ana Sayfa")
                        }

                        Button(onClick = { onNavigateSavedQuestions() }) {
                            Text("Kaydedilen Sorular")
                        }

                    }

                    Spacer(modifier = Modifier.height(24.dp))
                        LaunchedEffect(showResult) {
                            if (showResult) {
                                firestoreHelper.getBookmarkedQuestions(
                                    onSuccess = { fetched ->
                                        bookmarkedQuestions.clear()
                                        bookmarkedQuestions.addAll(fetched)
                                    },
                                    onFailure = { Log.e("QuizScreen", "Bookmark yüklenemedi: ${it.message}") }
                                )
                            }
                        }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Kaydedilen soruları önceden kontrol et
                        LaunchedEffect(Unit) {
                            firestoreHelper.getBookmarkedQuestions(
                                onSuccess = { bookmarkedQuestions ->
                                    questions.forEachIndexed { index, question ->
                                        val isBookmarked = bookmarkedQuestions.any {
                                            it.question == question.question && it.testId == testId
                                        }
                                        bookmarkedStates[index] = isBookmarked
                                    }
                                },
                                onFailure = {
                                    Log.e("QuizScreen", "Bookmarked sorular alınamadı: ${it.message}")
                                }
                            )
                        }

                        questions.forEachIndexed { index, question ->
                            val (selected, shown) = answers[index] ?: (null to false)
                            val isCorrect = selected == question.answer

                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Text("${index + 1}. ${question.question}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "Senin cevabın: ${selected ?: "Boş"}" +
                                            if (shown && selected != null) {
                                                if (isCorrect) "  ✔" else "  ✘"
                                            } else "",
                                    color = when {
                                        selected == null -> Color.Gray
                                        isCorrect -> Color(0xFF388E3C)
                                        else -> Color(0xFFD32F2F)
                                    },
                                    fontWeight = FontWeight.Medium
                                )
                                if (shown && selected != question.answer) {
                                    Text("Doğru cevap: ${question.answer} ✔", color = Color(0xFF1976D2))
                                }

                                // 🔖 Kaydet ikonunu göster (IconButton ile)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = {
                                            val isBookmarked = bookmarkedQuestions.any {
                                                it.question == question.question && it.testId == question.testId
                                            }
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(
                                                id = if (bookmarkedStates[index] == true)
                                                    R.drawable.bookmark_added else R.drawable.bookmark_add
                                            ),
                                            contentDescription = "Kaydet",
                                            tint = if (bookmarkedStates[index] == true) Color(0xFF1976D2) else Color.Gray
                                        )

                                    }
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                        }

                    }
                }
            } else {
                AnimatedContent(
                    targetState = currentIndex,
                    transitionSpec = { fadeIn() with fadeOut() }
                ) { index ->
                    val question = questions[index]
                    val (selected, shown) = answers[index] ?: (null to false)

                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("Soru ${index + 1}/${questions.size}", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(question.question, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(24.dp))

                        question.options.forEach { option ->
                            val isCorrect = option == question.answer
                            val isSelected = option == selected
                            val backgroundColor = when {
                                shown && isCorrect -> Color(0xFF4CAF50)
                                shown && isSelected && !isCorrect -> Color(0xFFF44336)
                                isSelected -> Color(0xFF1976D2)
                                else -> Color(0xFFE0E0E0)
                            }

                            val contentColor = if (isSelected || shown) Color.White else Color.Black

                            Button(
                                onClick = {
                                    if (!shown) answers[index] = option to false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !shown || selected == null
                            ) {
                                Text(
                                    text = option + if (shown && isCorrect) " ✔" else if (shown && isSelected && !isCorrect) " ✘" else "",
                                    color = contentColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { answers[index] = selected to true }, enabled = !shown) {
                            Text("Cevabı Göster")
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            if (index > 0) {
                                Button(onClick = { currentIndex-- }) { Text("Geri") }
                            }
                            Button(onClick = {
                                if (index < questions.lastIndex) currentIndex++ else showResult = true
                            }) {
                                Text(if (index < questions.lastIndex) "Sonraki" else "Testi Bitir")
                            }
                        }
                    }
                }
            }
        }
    }
}
