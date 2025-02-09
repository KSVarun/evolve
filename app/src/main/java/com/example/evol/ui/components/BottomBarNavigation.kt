package com.example.evol.ui.components

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController


sealed class TabItem(val title: String, val route: String, val icon: ImageVector) {
    data object Tracker : TabItem("Tracker", "tracker", Icons.Filled.Home)
    data object Timer : TabItem("Timer", "timer", Icons.Filled.Build)
    data object Remainder : TabItem("Remainder", "remainder", Icons.Filled.Notifications)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BottomBarNavigation(context: Context) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) {innerPadding->
        NavHost(navController, startDestination = TabItem.Tracker.route, modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            composable(TabItem.Tracker.route) { Tracker(context) }
            composable(TabItem.Timer.route) { Timer(context) }
            composable(TabItem.Remainder.route){ Remainder(context)}
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        TabItem.Tracker,
        TabItem.Timer,
        TabItem.Remainder
    )
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    NavigationBar(
        containerColor = Color.Gray,
        contentColor = Color.White
    ) {

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
