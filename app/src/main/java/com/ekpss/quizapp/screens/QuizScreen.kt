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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

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
    var isLoadingResults by remember { mutableStateOf(false) }

    // Bookmark durumlarını güncelleyen fonksiyon
    fun updateBookmarkStates() {
        firestoreHelper.getBookmarkedQuestions(
            onSuccess = { bookmarked ->
                bookmarkedQuestions.clear()
                bookmarkedQuestions.addAll(bookmarked)
                questions.forEachIndexed { index, question ->
                    val isBookmarked = bookmarked.any {
                        it.question == question.question && it.testId == testId
                    }
                    bookmarkedStates[index] = isBookmarked
                }
            },
            onFailure = {
                Log.e("QuizScreen", "Bookmark durumları yüklenemedi: ${it.message}")
            }
        )
    }

    LaunchedEffect(subject) {
        firestoreHelper.getQuestions(
            subject = subject,
            testId = testId,
            onSuccess = { fetchedQuestions ->
                questions = fetchedQuestions
                // Sorular yüklendiğinde bookmark durumlarını da yükle
                updateBookmarkStates()
            },
            onFailure = {
                Log.e("QuizScreen", "Soru çekilemedi: ${it.message}")
            }
        )
    }

    // Test sonuçları gösterildiğinde bookmark durumlarını güncelle
    LaunchedEffect(showResult) {
        if (showResult) {
            firestoreHelper.getBookmarkedQuestions(
                onSuccess = { bookmarked ->
                    bookmarkedQuestions.clear()
                    bookmarkedQuestions.addAll(bookmarked)
                    questions.forEachIndexed { index, question ->
                        bookmarkedStates[index] = bookmarked.any {
                            it.question == question.question && it.testId == testId
                        }
                    }
                },
                onFailure = { error ->
                    Log.e("QuizScreen", "Bookmark durumları yüklenemedi: ${error.message}")
                }
            )
        }
    }

    fun saveQuestionToFirestore(subject: String, question: Question) {
        firestoreHelper.saveBookmarkedQuestion(
            subject = subject,
            question = question.copy(testId = testId),
            onSuccess = {},
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
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when {
                            percentage >= 80 -> "Harika! 🎉"
                            percentage >= 50 -> "İyi! 👍"
                            else -> "Biraz Daha Çalışmalısın 💪"
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = resultColor
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Sonuçlar",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "$score / ${questions.size}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = resultColor
                            )
                            Text(
                                "(%$percentage)",
                                fontSize = 18.sp,
                                color = resultColor
                            )
                            
                            LinearProgressIndicator(
                                progress = percentage / 100f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .height(8.dp),
                                color = resultColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                currentIndex = 0
                                answers.clear()
                                savedQuestions.clear()
                                showResult = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Yeniden Dene")
                        }
                        Button(
                            onClick = onNavigateHome,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("Ana Sayfa")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Soru Detayları",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isLoadingResults) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(questions.size) { index ->
                                val question = questions[index]
                                val (selected, shown) = answers[index] ?: (null to false)
                                val isCorrect = selected == question.answer

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            isCorrect -> Color(0xFFE8F5E9)  // Açık yeşil
                                            selected != null -> Color(0xFFFFEBEE)  // Açık kırmızı
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "${index + 1}. Soru",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            IconButton(
                                                onClick = {
                                                    val isCurrentlyBookmarked = bookmarkedStates[index] == true
                                                    if (isCurrentlyBookmarked) {
                                                        firestoreHelper.deleteBookmarkedQuestion(
                                                            subject = subject,
                                                            question = question,
                                                            onSuccess = {
                                                                bookmarkedStates[index] = false
                                                                bookmarkedQuestions.removeAll { it.question == question.question && it.testId == testId }
                                                            },
                                                            onFailure = {
                                                                Log.e("QuizScreen", "Soru silinemedi: ${it.message}")
                                                            }
                                                        )
                                                    } else {
                                                        firestoreHelper.saveBookmarkedQuestion(
                                                            subject = subject,
                                                            question = question,
                                                            onSuccess = {
                                                                bookmarkedStates[index] = true
                                                                bookmarkedQuestions.add(question)
                                                            },
                                                            onFailure = {
                                                                Log.e("QuizScreen", "Soru kaydedilemedi: ${it.message}")
                                                            }
                                                        )
                                                    }
                                                }
                                            ) {
                                                val isBookmarked = bookmarkedStates[index] == true
                                                Icon(
                                                    painter = painterResource(
                                                        id = if (isBookmarked) R.drawable.bookmark_added 
                                                            else R.drawable.bookmark_add
                                                    ),
                                                    contentDescription = if (isBookmarked)
                                                        "Kaydedildi" else "Kaydet",
                                                    tint = if (isBookmarked)
                                                        Color(0xFF1976D2) else Color.Gray
                                                )
                                            }
                                        }
                                        
                                        Text(
                                            question.question,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )

                                        // Seçenekler
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            question.options.forEach { option ->
                                                val isCorrectOption = option == question.answer
                                                val isSelectedOption = option == selected
                                                
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 2.dp),
                                                    horizontalArrangement = Arrangement.Start,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        painter = painterResource(
                                                            id = when {
                                                                isCorrectOption -> R.drawable.check_circle
                                                                isSelectedOption -> R.drawable.cancel
                                                                else -> R.drawable.check_circle
                                                            }
                                                        ),
                                                        contentDescription = when {
                                                            isCorrectOption -> "Doğru Cevap"
                                                            isSelectedOption -> "Yanlış Cevap"
                                                            else -> "Seçenek"
                                                        },
                                                        tint = when {
                                                            isCorrectOption -> Color(0xFF388E3C)
                                                            isSelectedOption -> Color(0xFFD32F2F)
                                                            else -> Color.Gray
                                                        },
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = option,
                                                        color = when {
                                                            isCorrectOption -> Color(0xFF388E3C)
                                                            isSelectedOption -> Color(0xFFD32F2F)
                                                            else -> Color.Gray
                                                        },
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
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
