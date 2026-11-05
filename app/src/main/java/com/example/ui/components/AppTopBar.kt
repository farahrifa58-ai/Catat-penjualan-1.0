package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun AppTopBar(
    currentScreen: ScreenDestination,
    onNavigateBack: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSubScreen = currentScreen in listOf(
        ScreenDestination.PENGATURAN,
        ScreenDestination.TENTANG,
        ScreenDestination.DETAIL_TRANSAKSI,
        ScreenDestination.ATUR_HARGA,
        ScreenDestination.LAPAK
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (isSubScreen) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("topbar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = "App Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (currentScreen) {
                        ScreenDestination.SPLASH -> "Catat Jualan"
                        ScreenDestination.PENGATURAN -> "Pengaturan"
                        ScreenDestination.TENTANG -> "Tentang Catat Jualan"
                        ScreenDestination.DETAIL_TRANSAKSI -> "Detail Transaksi"
                        ScreenDestination.SETORAN -> "Catat Setoran"
                        ScreenDestination.PENJUALAN -> "Input Penjualan"
                        ScreenDestination.RIWAYAT -> "Riwayat & Laporan"
                        ScreenDestination.MENU -> "Daftar Menu Makanan"
                        ScreenDestination.LAPAK -> "Daftar Lapak"
                        ScreenDestination.ATUR_HARGA -> "Pengaturan Harga Lapak"
                        ScreenDestination.BERANDA -> "Catat Jualan"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (com.example.util.AppConfig.IS_TRIAL) "File APK: Versi Trial (30-40 Menit)" else "File APK: Full Version",
                    fontSize = 11.sp,
                    color = androidx.compose.ui.graphics.Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold
                )
            }

            if (!isSubScreen) {
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("topbar_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Pengaturan",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
