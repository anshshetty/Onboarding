package dev.ansh.onboarding.onboarding.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.ansh.onboarding.jaronboarding.presentation.landing.LandingScreen
import dev.ansh.onboarding.onboarding.ui.onboarding.screen.OnboardingScreen
import dev.ansh.onboarding.onboarding.ui.onboarding.viewmodel.OnboardingViewModel

/**
 * Navigation routes
 */
object Routes {
    const val ONBOARDING = "onboarding"
    const val LANDING = "landing"
}

/**
 * Main navigation graph
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    onboardingViewModel: OnboardingViewModel,
    finishActivity: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Routes.ONBOARDING
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                viewModel = onboardingViewModel,
                onNavigateBack = {
                    // If there's a previous destination, navigate back
                    // Otherwise, finish the activity (handled in MainActivity)
                    if (!navController.popBackStack()) {
                        finishActivity()
                    }
                },
                onNavigateToLanding = {
                    navController.navigate(Routes.LANDING) {
                        // Clear the onboarding screen from the back stack
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.LANDING) {
            LandingScreen()
        }
    }
}
