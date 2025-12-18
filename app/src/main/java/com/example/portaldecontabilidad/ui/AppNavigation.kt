package com.example.portaldecontabilidad.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.portaldecontabilidad.ui.detail.TaskDetailScreen
import com.example.portaldecontabilidad.ui.home.HomeScreen
import com.example.portaldecontabilidad.ui.newtask.NewTaskScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onTaskClick = { taskId -> navController.navigate("taskDetail/$taskId") },
                onAddTaskClick = { navController.navigate("newTask") }
            )
        }
        composable(
            route = "taskDetail/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) {
            TaskDetailScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("newTask") {
            NewTaskScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
