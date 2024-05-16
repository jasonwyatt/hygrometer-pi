package us.jwf.hygrometer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import us.jwf.hygrometer.home.homeDestination
import us.jwf.hygrometer.plant.plantDestination
import us.jwf.hygrometer.ui.theme.HygrometerTheme
import us.jwf.hygrometer.ui.theme.Typography
import us.jwf.hygrometer.util.destination

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            CompositionLocalProvider(
                LocalNavController provides navController,
                LocalApp provides application as App,
            ) {
                HygrometerTheme {
                    NavHost(navController = navController, startDestination = "home") {
                        homeDestination()
                        plantDestination()
                    }
                }
            }
        }
    }
}

val LocalNavController = compositionLocalOf<NavController> { error("Setup") }
val LocalApp = compositionLocalOf<App> { error("Not set") }
