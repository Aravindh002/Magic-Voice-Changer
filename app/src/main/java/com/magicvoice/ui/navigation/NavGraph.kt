package com.magicvoice.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.magicvoice.ui.call.CallScreen
import com.magicvoice.ui.dialpad.DialPadScreen
import com.magicvoice.ui.home.HomeScreen
import com.magicvoice.ui.splash.SplashScreen
import com.magicvoice.ui.subscription.SubscriptionScreen
import com.magicvoice.ui.voiceeffects.VoiceEffectsScreen

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object VoiceEffects : Screen("voice_effects")
    data object DialPad : Screen("dial_pad")
    data object Call : Screen("call/{phoneNumber}") {
        fun createRoute(phoneNumber: String) = "call/$phoneNumber"
    }
    data object Subscription : Screen("subscription")
}

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToDialPad = { navController.navigate(Screen.DialPad.route) },
                onNavigateToVoiceEffects = { navController.navigate(Screen.VoiceEffects.route) },
                onNavigateToSubscription = { navController.navigate(Screen.Subscription.route) }
            )
        }
        composable(Screen.VoiceEffects.route) { VoiceEffectsScreen { navController.popBackStack() } }
        composable(Screen.DialPad.route) {
            DialPadScreen(onNavigateBack = { navController.popBackStack() }, onCallInitiated = { navController.navigate(Screen.Call.createRoute(it)) })
        }
        composable(Screen.Call.route) { backStackEntry ->
            CallScreen(phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: "", onCallEnded = { navController.popBackStack() })
        }
        composable(Screen.Subscription.route) { SubscriptionScreen { navController.popBackStack() } }
    }
}
