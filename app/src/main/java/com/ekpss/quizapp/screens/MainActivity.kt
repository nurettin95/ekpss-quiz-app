package com.ekpss.quizapp.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.ekpss.quizapp.ui.theme.EkpssQuizAppTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EkpssQuizAppTheme {

                // Firebase'den mevcut kullanıcıyı kontrol et
                var isLoggedIn by remember {
                    mutableStateOf(FirebaseAuth.getInstance().currentUser != null)
                }

                if (isLoggedIn) {
                    MainAppContent()
                } else {
                    LoginScreen {
                        isLoggedIn = true
                    }
                }
            }
        }
    }
}