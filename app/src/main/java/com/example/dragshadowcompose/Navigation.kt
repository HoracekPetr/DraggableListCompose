package com.example.dragshadowcompose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                modifier = Modifier.fillMaxSize(),
                dragListItems = users,
                indicatorContent = { dragInfo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            modifier = Modifier.padding(12.dp),
                            text = dragInfo.data?.name.orEmpty()
                        )
                    }
                },
                dragListItem = { user ->
                    Card(
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(horizontal = 16.dp, vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = user.name, fontSize = 24.sp)
                        }
                    }
                },
                onDragStop = {
                    navController.navigate(
                        NavDestination.DETAIL.route.replace(
                            "{id}",
                            it?.name.orEmpty()
                        )
                    )
                }
            )
        }
        composable(NavDestination.DETAIL.route, arguments = listOf(navArgument("id") {
            defaultValue = null
            nullable = true
        })) { backStackEntry ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "${backStackEntry.arguments?.getString("id")}")
            }
        }
    }
}