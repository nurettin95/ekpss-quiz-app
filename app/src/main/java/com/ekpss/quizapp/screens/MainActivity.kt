package com.ekpss.quizapp.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.ekpss.quizapp.ui.theme.EkpssQuizAppTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import android.widget.Toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EkpssQuizAppTheme {
                var isLoggedIn by remember {
                    mutableStateOf(FirebaseAuth.getInstance().currentUser != null)
                }

                val scope = rememberCoroutineScope()

                // Geri tuşu yönetimi
                DisposableEffect(isLoggedIn) {
                    val callback = object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            if (!isLoggedIn) { // Login ekranındaysa
                                if (doubleBackToExitPressedOnce) {
                                    // İkinci basışta uygulamadan çık
                                    finish()
                                } else {
                                    // İlk basışta uyarı göster
                                    doubleBackToExitPressedOnce = true
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Çıkmak için tekrar basın",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // 2 saniye içinde tekrar basılmazsa sıfırla
                                    scope.launch {
                                        delay(2000)
                                        doubleBackToExitPressedOnce = false
                                    }
                                }
                            }
                        }
                    }

                    onBackPressedDispatcher.addCallback(callback)
                    onDispose {
                        callback.remove()
                    }
                }

                if (isLoggedIn) {
                    MainAppContent(
                        onLogout = {
                            isLoggedIn = false
                        }
                    )
                } else {
                    LoginScreen {
                        isLoggedIn = true
                    }
                }
            }
        }
    }
}