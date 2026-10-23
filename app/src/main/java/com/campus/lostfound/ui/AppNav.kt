package com.campus.lostfound.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.campus.lostfound.CampusApp
import com.campus.lostfound.Session
import com.campus.lostfound.ui.screens.ChatScreen
import com.campus.lostfound.ui.screens.DetailScreen
import com.campus.lostfound.ui.screens.HomeScreen
import com.campus.lostfound.ui.screens.LoginScreen
import com.campus.lostfound.ui.screens.PostScreen
import com.campus.lostfound.ui.screens.ProfileScreen

object Routes {
    const val Login = "login"
    const val Home = "home"
    const val Post = "post"
    const val Chat = "chat"
    const val Profile = "profile"
    const val Detail = "detail/{id}"
    fun detail(id: String) = "detail/$id"
}

@Composable
fun AppNav(nav: NavHostController = rememberNavController()) {
    val start = if (Session.token.isNullOrBlank()) Routes.Login else Routes.Home
    NavHost(navController = nav, startDestination = start) {
        composable(Routes.Login) {
            LoginScreen(
                onLoggedIn = {
                    nav.navigate(Routes.Home) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.Home) {
            HomeScreen(
                onPost = { nav.navigate(Routes.Post) },
                onChat = { nav.navigate(Routes.Chat) },
                onProfile = { nav.navigate(Routes.Profile) },
                onItem = { id -> nav.navigate(Routes.detail(id)) },
                onLogout = {
                    Session.save(CampusApp.instance, null)
                    nav.navigate(Routes.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.Post) {
            PostScreen(
                onBack = { nav.popBackStack() },
                onPosted = {
                    nav.popBackStack()
                },
            )
        }
        composable(Routes.Chat) {
            ChatScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.Profile) {
            ProfileScreen(
                onBack = { nav.popBackStack() },
                onLogout = {
                    Session.save(CampusApp.instance, null)
                    nav.navigate(Routes.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.Detail) { entry ->
            val id = entry.arguments?.getString("id") ?: return@composable
            DetailScreen(
                itemId = id,
                onBack = { nav.popBackStack() },
            )
        }
    }
}
