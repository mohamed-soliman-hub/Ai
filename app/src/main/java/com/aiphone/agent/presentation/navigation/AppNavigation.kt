package com.aiphone.agent.presentation.navigation
import androidx.compose.animation.*; import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.*; import androidx.navigation.compose.*
import com.aiphone.agent.presentation.chat.ChatScreen
import com.aiphone.agent.presentation.macros.MacroScreen
import com.aiphone.agent.presentation.onboarding.OnboardingScreen
import com.aiphone.agent.presentation.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Chat : Screen("chat/{conversationId}") { fun create(id: String = "new") = "chat/$id" }
    data object Settings : Screen("settings")
    data object Macros : Screen("macros")
}

@Composable
fun AppNavigation(isOnboardingComplete: Boolean) {
    val nav = rememberNavController()
    val start = if (isOnboardingComplete) Screen.Chat.create() else Screen.Onboarding.route
    NavHost(nav, startDestination = start,
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(280)) + fadeIn(tween(280)) },
        exitTransition  = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(280)) + fadeOut(tween(280)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(280)) + fadeIn(tween(280)) },
        popExitTransition  = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(280)) + fadeOut(tween(280)) }
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onComplete = { nav.navigate(Screen.Chat.create()) { popUpTo(Screen.Onboarding.route) { inclusive = true } } })
        }
        composable(Screen.Chat.route, listOf(navArgument("conversationId") { type = NavType.StringType })) {
            ChatScreen(
                conversationId = it.arguments?.getString("conversationId") ?: "new",
                onNavigateToSettings = { nav.navigate(Screen.Settings.route) },
                onNavigateToMacros = { nav.navigate(Screen.Macros.route) },
                onNewConversation = { nav.navigate(Screen.Chat.create()) { popUpTo(Screen.Chat.route) { inclusive = true } } }
            )
        }
        composable(Screen.Settings.route) { SettingsScreen(onBack = { nav.popBackStack() }) }
        composable(Screen.Macros.route)  { MacroScreen(onBack = { nav.popBackStack() }, onRunMacro = {}) }
    }
}