package com.example.dragshadowcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.dragshadowcompose.ui.theme.DragShadowComposeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DragShadowComposeTheme {
                AppNavHost()
            }
        }
    }
}

val users = List(30) {
    User(
        id = "userId $it",
        name = when {
            it % 2 == 0 -> "Michael"
            it % 5 == 0 -> "Saul"
            it % 7 == 0 -> "Walter"
            it % 13 == 0 -> "Jesse"
            else -> "Gus"
        }
    )
}

data class User(
    val id: String,
    val name: String
)