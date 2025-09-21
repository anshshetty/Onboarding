package dev.ansh.onboarding.onboarding

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dev.ansh.onboarding.onboarding.di.NetworkModule
import dev.ansh.onboarding.onboarding.navigation.NavGraph
import dev.ansh.onboarding.onboarding.ui.onboarding.OnboardingViewModel
import dev.ansh.onboarding.onboarding.ui.theme.JarOnboardingTheme

/**
 * Main activity that hosts the navigation graph
 */
class MainActivity : ComponentActivity() {
    
    private lateinit var onboardingViewModel: OnboardingViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize dependencies
        setupDependencies()
        
        setContent {
            JarOnboardingTheme {
                val systemUiController = rememberSystemUiController()
                val navController = rememberNavController()
                
                // Set transparent status bar
                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = Color.Transparent,
                        darkIcons = false
                    )
                    systemUiController.setNavigationBarColor(
                        color = Color.Black,
                        darkIcons = false
                    )
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        navController = navController,
                        onboardingViewModel = onboardingViewModel
                    )
                }
            }
        }
    }
    
    /**
     * Simple dependency injection setup
     */
    private fun setupDependencies() {
        val okHttpClient = NetworkModule.provideOkHttpClient(cacheDir)
        val retrofit = NetworkModule.provideRetrofit(okHttpClient)
        val educationApi = NetworkModule.provideEducationApi(retrofit)
        val educationRepository = NetworkModule.provideEducationRepository(educationApi)
        
        onboardingViewModel = OnboardingViewModel(educationRepository)
    }
}
