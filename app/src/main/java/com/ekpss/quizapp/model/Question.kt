package com.ekpss.quizapp.model

data class Question(
    val question: String = "",
    val options: List<String> = emptyList(),
    val answer: String = "",
    val testId: String = ""
)