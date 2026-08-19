package com.voxai.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.voxai.app.ui.screens.CallScreen
import com.voxai.app.ui.screens.ConnectScreen
import com.voxai.app.ui.screens.HistoryScreen
import com.voxai.app.viewmodel.CallViewModel

sealed class Screen(val route: String) {
    object Connect : Screen("connect")
    object Call : Screen("call")
    object History : Screen("history")
}

@Composable
fun VoxAINavGraph(
    navController: NavHostController = rememberNavController(),
    viewModel: CallViewModel = viewModel(),
) {
    NavHost(navController = navController, startDestination = Screen.Connect.route) {
        composable(Screen.Connect.route) {
            ConnectScreen(
                viewModel = viewModel,
                onConnect = { room, user ->
                    viewModel.connectToRoom(room, user)
                    navController.navigate(Screen.Call.route)
                },
                onHistory = { navController.navigate(Screen.History.route) },
            )
        }

        composable(Screen.Call.route) {
            CallScreen(
                viewModel = viewModel,
                onEndCall = {
                    viewModel.endCall()
                    navController.popBackStack(Screen.Connect.route, inclusive = false)
                },
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
