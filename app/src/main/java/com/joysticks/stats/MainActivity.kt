package com.joysticks.stats

import android.graphics.Color
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.widget.Toast
import com.joysticks.stats.engine.GameViewModel
import com.joysticks.stats.utils.PdfExportUtils
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.joysticks.stats.ui.navigation.AppNavigation
import com.joysticks.stats.ui.theme.JoysticksStatsTheme
import com.joysticks.stats.ui.theme.HudBackground
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

import com.joysticks.stats.data.SettingsDataStore
import com.joysticks.stats.data.GameDataStore
import com.joysticks.stats.utils.NetworkUtils
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val forceImmersiveMode = true

    private val gameViewModel: GameViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GameViewModel(GameDataStore(applicationContext)) as T
            }
        }
    }

    private val createPdfDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        uri?.let {
            PdfExportUtils.exportStatsSheetToPdf(this, it, gameViewModel.gameState, gameViewModel.gameState.roster)
        } ?: Toast.makeText(this, "Exportation PDF annulée", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (forceImmersiveMode) enterImmersiveMode(window)

        // Initialize NetworkUtils with saved SSID
        val settingsDataStore = SettingsDataStore(this)
        lifecycleScope.launch {
            settingsDataStore.targetSsidFlow.collect { ssid ->
                NetworkUtils.targetSsid = ssid
            }
        }

        setContent {

            JoysticksStatsTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = HudBackground
                ) {

                    AppNavigation(gameViewModel = gameViewModel, createPdfDocumentLauncher = createPdfDocumentLauncher)

                }

            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (forceImmersiveMode) enterImmersiveMode(window)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (forceImmersiveMode && hasFocus) enterImmersiveMode(window)
    }

    private fun enterImmersiveMode(window: Window) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
