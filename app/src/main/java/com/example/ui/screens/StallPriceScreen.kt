package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.StallEntity
import com.example.ui.theme.StatusGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.util.FormatUtils

@Composable
fun StallPriceScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val strings = viewModel.strings
    val activeMenus by viewModel.activeMenuItems.collectAsStateWithLifecycle()
    val stalls by viewModel.activeStalls.collectAsStateWithLifecycle()
    val allPrices by viewModel.allStallPrices.collectAsStateWithLifecycle()

    var selectedMenuIndex by remember { mutableIntStateOf(0) }

    val currentMenu: MenuItemEntity? = activeMenus.getOrNull(selectedMenuIndex)

    Column(modifier = modifier.fillMaxSize()) {
        // Notice Info
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Pilih menu makanan, kemudian atur harga jual khusus untuk masing-masing lapak.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (activeMenus.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(strings.menuKosong, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return
        }

        // Horizontal Menu Selector Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedMenuIndex.coerceIn(0, (activeMenus.size - 1).coerceAtLeast(0)),
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            activeMenus.forEachIndexed { index, menu ->
                Tab(
                    selected = selectedMenuIndex == index,
                    onClick = { selectedMenuIndex = index },
                    text = {
                        Text(
                            text = menu.name,
                            fontWeight = if (selectedMenuIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("price_tab_menu_${menu.id}")
                )
            }
        }

        currentMenu?.let { menu ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Harga untuk ${menu.name} (Harga Standar: ${FormatUtils.formatRupiah(menu.price)} / ${menu.unit})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                if (stalls.isEmpty()) {
                    item {
                        Text(strings.lapakKosong, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(stalls, key = { "stall_${it.id}" }) { stall ->
                        val savedPriceEntity = allPrices.find { it.stallId == stall.id && it.menuId == menu.id }
                        val currentPrice = savedPriceEntity?.price ?: menu.price

                        StallPriceRowCard(
                            stall = stall,
                            menu = menu,
                            savedPrice = currentPrice,
                            onSavePrice = { newPrice ->
                                viewModel.setStallPrice(stall.id, menu.id, newPrice)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StallPriceRowCard(
    stall: StallEntity,
    menu: MenuItemEntity,
    savedPrice: Double,
    onSavePrice: (Double) -> Unit
) {
    var priceText by remember(savedPrice) {
        mutableStateOf(savedPrice.toLong().toString())
    }
    var isDirty by remember(savedPrice) { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("price_row_stall_${stall.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stall.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (stall.address.isNotBlank()) {
                    Text(
                        text = stall.address,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = priceText,
                onValueChange = {
                    priceText = it.filter { ch -> ch.isDigit() }
                    isDirty = true
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                prefix = { Text("Rp ", fontSize = 12.sp) },
                modifier = Modifier
                    .width(130.dp)
                    .testTag("price_input_stall_${stall.id}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    val p = priceText.toDoubleOrNull() ?: 0.0
                    if (p > 0) {
                        onSavePrice(p)
                        isDirty = false
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDirty) MaterialTheme.colorScheme.primary else StatusGreen
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("price_save_stall_${stall.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Simpan",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
