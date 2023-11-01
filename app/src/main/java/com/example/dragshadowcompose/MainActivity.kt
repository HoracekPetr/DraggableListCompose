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

val texts = List(30) {
    "Item $it"
}