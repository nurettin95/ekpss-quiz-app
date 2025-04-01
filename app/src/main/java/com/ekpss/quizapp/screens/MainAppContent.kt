package com.ekpss.quizapp.screens

import TestListScreen
import androidx.compose.runtime.*
import com.ekpss.quizapp.ui.theme.EkpssQuizAppTheme

@Composable
fun MainAppContent() {
    EkpssQuizAppTheme {
        var currentScreen by remember { mutableStateOf("subjectList") }
        var selectedSubject by remember { mutableStateOf<String?>(null) }
        var selectedTest by remember { mutableStateOf<String?>(null) }

        when (currentScreen) {
            "subjectList" -> SubjectListScreen { subject ->
                selectedSubject = subject
                currentScreen = "subjectDetail"
            }

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
