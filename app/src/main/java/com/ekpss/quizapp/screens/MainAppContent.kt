package com.ekpss.quizapp.screens

import TestListScreen
import androidx.compose.runtime.*
import com.ekpss.quizapp.ui.theme.EkpssQuizAppTheme
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.saveable.rememberSaveable
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainAppContent(onLogout: () -> Unit = {}) {
    EkpssQuizAppTheme {
        var currentScreen by rememberSaveable { mutableStateOf("subjectList") }
        var selectedSubject by rememberSaveable { mutableStateOf<String?>(null) }
        var selectedTest by rememberSaveable { mutableStateOf<String?>(null) }
        
        // Geri tuşu için gerekli değişkenler
        var doubleBackToExitPressedOnce by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        // Geri tuşu yönetimi
        val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
        
        DisposableEffect(currentScreen) {
            val callback = object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    when (currentScreen) {
                        "subjectList" -> {
                            if (doubleBackToExitPressedOnce) {
                                // İkinci basışta uygulamadan çık
                                isEnabled = false
                                android.os.Process.killProcess(android.os.Process.myPid())
                            } else {
                                // İlk basışta uyarı göster
                                doubleBackToExitPressedOnce = true
                                Toast.makeText(context, "Çıkmak için tekrar basın", Toast.LENGTH_SHORT).show()
                                
                                // 2 saniye içinde tekrar basılmazsa sıfırla
                                scope.launch {
                                    delay(2000)
                                    doubleBackToExitPressedOnce = false
                                }
                            }
                        }
                        "subjectDetail" -> {
                            currentScreen = "subjectList"
                            selectedSubject = null
                        }
                        "noteScreen" -> {
                            currentScreen = "subjectDetail"
                        }
                        "testList" -> {
                            currentScreen = "subjectDetail"
                        }
                        "quizScreen" -> {
                            currentScreen = "testList"
                        }
                        "savedQuestions" -> {
                            currentScreen = "subjectDetail"
                        }
                    }
                }
            }
            
            backDispatcher?.addCallback(callback)
            onDispose {
                callback.remove()
            }
        }

        when (currentScreen) {
            "subjectList" -> SubjectListScreen(
                onSubjectClick = { subject ->
                    selectedSubject = subject
                    currentScreen = "subjectDetail"
                },
                onLogout = onLogout
            )

            "subjectDetail" -> SubjectDetailScreen(
                subject = selectedSubject!!,
                onBack = {
                    currentScreen = "subjectList"
                    selectedSubject = null
                },
                onNoteClick = {
                    currentScreen = "noteScreen"
                },
                onQuizClick = {
                    currentScreen = "testList"
                },
                onSavedQuestionsClick = {
                    currentScreen = "savedQuestions"
                }
            )

            "noteScreen" -> NoteScreen(
                subject = selectedSubject!!,
                onBack = { currentScreen = "subjectDetail" }
            )

            "testList" -> TestListScreen(
                subject = selectedSubject!!,
                onBack = { currentScreen = "subjectDetail" },
                onTestSelected = { testId ->
                    selectedTest = testId
                    currentScreen = "quizScreen"
                }
            )

            "quizScreen" -> QuizScreen(
                subject = selectedSubject!!,
                testId = selectedTest!!,
                onBack = { currentScreen = "testList" },
                onNavigateHome = { currentScreen = "subjectList" },
                onNavigateSavedQuestions = { currentScreen = "savedQuestions" }
            )

            "savedQuestions" -> SavedQuestionsScreen(
                subject = selectedSubject!!,
                onBack = { currentScreen = "subjectDetail" }
            )
        }
    }
}