package com.example.dragshadowcompose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dragshadowcompose.ui.DragList

sealed class NavDestination(val route: String) {
    object DRAG : NavDestination("drag")
    object DETAIL : NavDestination("detail?id={id}")
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = NavDestination.DRAG.route) {
        composable(NavDestination.DRAG.route) {
            DragList(
                onNavigate = {
                    navController.navigate(NavDestination.DETAIL.route.replace("{id}", it))
                }
            )
        }
        composable(NavDestination.DETAIL.route, arguments = listOf(navArgument("id") {
            defaultValue = null
            nullable = true
        })) { backStackEntry ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Detail ${backStackEntry.arguments?.getString("id")}")
            }
        }
    }
}