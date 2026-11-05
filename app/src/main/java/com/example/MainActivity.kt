package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.preferences.AppThemeMode
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.DepositScreen
import com.example.ui.screens.DetailTransactionScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.SalesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StallPriceScreen
import com.example.ui.screens.StallScreen
import com.example.ui.theme.CatatJualanTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDarkTheme = themeMode == AppThemeMode.DARK

            CatatJualanTheme(darkTheme = isDarkTheme) {
                CatatJualanApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CatatJualanApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val strings = viewModel.strings
    val snackbarHostState = remember { SnackbarHostState() }

    val isTrialExpired by viewModel.isTrialExpired.collectAsStateWithLifecycle()

    // Listen for toast/snackbar messages
    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Periodic check for trial expiration (every 30 seconds)
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.checkTrialStatus()
            kotlinx.coroutines.delay(30000)
        }
    }

    // Show locking dialog when trial period has ended
    if (viewModel.isTrial && isTrialExpired) {
        com.example.ui.components.TrialExpiredDialog()
    }

    // Splash screen rendered standalone in full screen
    if (currentScreen == ScreenDestination.SPLASH) {
        SplashScreen(
            onTimeout = {
                viewModel.navigateTo(ScreenDestination.BERANDA)
            }
        )
        return
    }

    // Handle Android system back button
    BackHandler(enabled = currentScreen != ScreenDestination.BERANDA) {
        when (currentScreen) {
            ScreenDestination.TENTANG -> viewModel.navigateTo(ScreenDestination.PENGATURAN)
            ScreenDestination.PENGATURAN -> viewModel.navigateTo(ScreenDestination.BERANDA)
            ScreenDestination.DETAIL_TRANSAKSI -> viewModel.navigateTo(ScreenDestination.RIWAYAT)
            ScreenDestination.LAPAK -> viewModel.navigateTo(ScreenDestination.PENGATURAN)
            ScreenDestination.ATUR_HARGA -> viewModel.navigateTo(ScreenDestination.PENGATURAN)
            else -> viewModel.navigateTo(ScreenDestination.BERANDA)
        }
    }

    val isSubScreen = currentScreen in listOf(
        ScreenDestination.PENGATURAN,
        ScreenDestination.TENTANG,
        ScreenDestination.DETAIL_TRANSAKSI,
        ScreenDestination.ATUR_HARGA,
        ScreenDestination.LAPAK
    )

    Scaffold(
        topBar = {
            AppTopBar(
                currentScreen = currentScreen,
                onNavigateBack = {
                    when (currentScreen) {
                        ScreenDestination.TENTANG -> viewModel.navigateTo(ScreenDestination.PENGATURAN)
                        ScreenDestination.PENGATURAN -> viewModel.navigateTo(ScreenDestination.BERANDA)
                        ScreenDestination.DETAIL_TRANSAKSI -> viewModel.navigateTo(ScreenDestination.RIWAYAT)
                        ScreenDestination.LAPAK -> viewModel.navigateTo(ScreenDestination.PENGATURAN)
                        ScreenDestination.ATUR_HARGA -> viewModel.navigateTo(ScreenDestination.PENGATURAN)
                        else -> viewModel.navigateTo(ScreenDestination.BERANDA)
                    }
                },
                onOpenSettings = {
                    viewModel.navigateTo(ScreenDestination.PENGATURAN)
                }
            )
        },
        bottomBar = {
            if (!isSubScreen) {
                AppBottomBar(
                    currentScreen = currentScreen,
                    strings = strings,
                    onNavigate = { dest -> viewModel.navigateTo(dest) }
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                    ScreenDestination.SPLASH -> { /* Handled above */ }
                    ScreenDestination.BERANDA -> HomeScreen(viewModel = viewModel)
                    ScreenDestination.SETORAN -> DepositScreen(viewModel = viewModel)
                    ScreenDestination.PENJUALAN -> SalesScreen(viewModel = viewModel)
                    ScreenDestination.RIWAYAT -> HistoryScreen(viewModel = viewModel)
                    ScreenDestination.MENU -> MenuScreen(viewModel = viewModel)
                    ScreenDestination.LAPAK -> StallScreen(viewModel = viewModel)
                    ScreenDestination.ATUR_HARGA -> StallPriceScreen(viewModel = viewModel)
                    ScreenDestination.PENGATURAN -> SettingsScreen(
                        viewModel = viewModel,
                        onOpenAbout = { viewModel.navigateTo(ScreenDestination.TENTANG) }
                    )
                    ScreenDestination.TENTANG -> AboutScreen(viewModel = viewModel)
                    ScreenDestination.DETAIL_TRANSAKSI -> DetailTransactionScreen(
                        viewModel = viewModel,
                        onNavigateBack = { viewModel.navigateTo(ScreenDestination.RIWAYAT) }
                    )
                }
            }
        }
    }
}
