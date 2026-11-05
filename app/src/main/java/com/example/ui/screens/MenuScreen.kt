package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.StallEntity
import com.example.data.local.entity.StallMenuPriceEntity
import com.example.ui.theme.StatusGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.util.FormatUtils
import com.example.util.ImageStorageHelper
import java.io.File

@Composable
fun MenuScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val strings = viewModel.strings
    val menuItems by viewModel.allMenuItems.collectAsStateWithLifecycle()
    val activeMenus by viewModel.activeMenuItems.collectAsStateWithLifecycle()
    val stalls by viewModel.allStalls.collectAsStateWithLifecycle()
    val allPrices by viewModel.allStallPrices.collectAsStateWithLifecycle()

    // Tab state: 0 = Daftar Menu, 1 = Daftar Lapak
    var selectedFeatureTab by remember { mutableIntStateOf(0) }

    // Dialog states for Menu
    var showAddEditMenuDialog by remember { mutableStateOf(false) }
    var editingMenuItem by remember { mutableStateOf<MenuItemEntity?>(null) }
    var menuItemToDelete by remember { mutableStateOf<MenuItemEntity?>(null) }

    // Dialog states for Stall
    var showAddEditStallDialog by remember { mutableStateOf(false) }
    var editingStall by remember { mutableStateOf<StallEntity?>(null) }
    var stallToDelete by remember { mutableStateOf<StallEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            // ONLY ONE BUTTON AT THE BOTTOM, as requested by the user!
            if (selectedFeatureTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = {
                        editingMenuItem = null
                        showAddEditMenuDialog = true
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Tambah Menu", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("menu_fab_add")
                )
            } else {
                ExtendedFloatingActionButton(
                    onClick = {
                        editingStall = null
                        showAddEditStallDialog = true
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Tambah Lapak", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("stall_fab_add")
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // === TWO BUTTON SWITCH BAR: [🍽️ DAFTAR MENU] | [🏪 DAFTAR LAPAK] ===
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Button 1: Daftar Menu
                    Button(
                        onClick = { selectedFeatureTab = 0 },
                        shape = RoundedCornerShape(12.dp),
                        colors = if (selectedFeatureTab == 0) {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        contentPadding = PaddingValues(vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_tab_daftar_menu")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Daftar Menu",
                            fontWeight = if (selectedFeatureTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }

                    // Button 2: Daftar Lapak
                    Button(
                        onClick = { selectedFeatureTab = 1 },
                        shape = RoundedCornerShape(12.dp),
                        colors = if (selectedFeatureTab == 1) {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        contentPadding = PaddingValues(vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_tab_daftar_lapak")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Daftar Lapak",
                            fontWeight = if (selectedFeatureTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // === TAB CONTENT ===
            if (selectedFeatureTab == 0) {
                // ==========================================
                // SUB-FEATURE 1: DAFTAR MENU MAKANAN
                // ==========================================
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Title (NO BUTTON ON TOP, as requested!)
                    item {
                        Column(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(
                                text = "Menu Makanan",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${menuItems.size} jenis menu terdaftar",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Info Notice Card
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
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
                                    text = "Setiap menu dapat ditambahkan foto, HPP (modal), harga standar, serta harga jual khusus untuk masing-masing lapak.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    if (menuItems.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth().padding(32.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Fastfood,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "Belum Ada Menu Makanan",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Silakan klik tombol '+ Tambah Menu' di bawah untuk menambahkan menu makanan Anda.",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        // Food cards (spacious, clean layout, no cramped text columns!)
                        items(menuItems, key = { "menu_${it.id}" }) { item ->
                            val menuStallPrices = allPrices.filter { it.menuId == item.id }

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("menu_item_${item.id}")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    // Row 1: Photo, Name & Actions (Switch, Edit, Delete)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Photo / Icon
                                        if (!item.photoUri.isNullOrBlank() && File(item.photoUri).exists()) {
                                            AsyncImage(
                                                model = File(item.photoUri),
                                                contentDescription = item.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                            )
                                        } else {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(
                                                        if (item.isActive) MaterialTheme.colorScheme.primaryContainer
                                                        else MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Fastfood,
                                                    contentDescription = null,
                                                    tint = if (item.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        // Food Name
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = if (item.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                                            )
                                            Text(
                                                text = if (item.isActive) "Menu Aktif" else "Nonaktif",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (item.isActive) StatusGreen else MaterialTheme.colorScheme.outline
                                            )
                                        }

                                        // Controls: Switch, Edit, Delete
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Switch(
                                                checked = item.isActive,
                                                onCheckedChange = { viewModel.toggleMenuItemActive(item) },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                                ),
                                                modifier = Modifier.padding(end = 4.dp)
                                            )

                                            IconButton(
                                                onClick = {
                                                    editingMenuItem = item
                                                    showAddEditMenuDialog = true
                                                },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = strings.editMenu,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { menuItemToDelete = item },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = strings.hapusMenu,
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Row 2: 3-Pill Section (Full width, clean stats!)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Column {
                                                Text(text = "Satuan", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(text = item.unit, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1.3f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Column {
                                                Text(text = "HPP (Modal)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(text = FormatUtils.formatRupiah(item.hpp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1.4f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Column {
                                                Text(text = "Harga Standar", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                                Text(
                                                    text = FormatUtils.formatRupiah(item.price),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }

                                    // Row 3: Prices per stall breakdown
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        if (menuStallPrices.isEmpty()) {
                                            Text(
                                                text = "Harga standar (${FormatUtils.formatRupiah(item.price)}) berlaku di semua lapak.",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = "Harga Khusus di Lapak:",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                val priceBadges = menuStallPrices.map { sp ->
                                                    val sName = stalls.find { it.id == sp.stallId }?.name ?: "Lapak"
                                                    "$sName: ${FormatUtils.formatRupiah(sp.price)}"
                                                }.joinToString("  •  ")
                                                Text(
                                                    text = priceBadges,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // SUB-FEATURE 2: DAFTAR LAPAK & HARGA PRODUK
                // ==========================================
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Title (NO BUTTON ON TOP, as requested!)
                    item {
                        Column(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(
                                text = "Daftar Lapak & Harga",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${stalls.size} lapak terdaftar",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Notice Banner: Automatic price following explanation
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
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
                                    text = "Atur harga jual produk untuk setiap lapak di bawah ini. Saat Anda memilih lapak di Setoran, harga jual produk akan otomatis mengikuti harga lapak yang sudah diatur di sini.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    if (stalls.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth().padding(32.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Storefront,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "Belum Ada Lapak Terdaftar",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Klik tombol '+ Tambah Lapak' di bawah untuk membuat lapak jualan pertama Anda.",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(stalls, key = { "stall_full_${it.id}" }) { stall ->
                            StallWithMenuPricesCard(
                                stall = stall,
                                activeMenus = activeMenus,
                                allPrices = allPrices,
                                strings = strings,
                                onEditStall = {
                                    editingStall = stall
                                    showAddEditStallDialog = true
                                },
                                onDeleteStall = {
                                    stallToDelete = stall
                                },
                                onToggleActive = {
                                    viewModel.toggleStallActive(stall)
                                },
                                onSavePrice = { menuId, newPrice ->
                                    viewModel.setStallPrice(stall.id, menuId, newPrice)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog: Add / Edit Menu Makanan
    if (showAddEditMenuDialog) {
        AddEditMenuFullDialog(
            item = editingMenuItem,
            stalls = stalls,
            allPrices = allPrices,
            strings = strings,
            onDismiss = { showAddEditMenuDialog = false },
            onUpdateStallNameAndPhoto = { stallId, newName, newPhotoUri ->
                viewModel.updateStallNameAndPhoto(stallId, newName, newPhotoUri)
            },
            onAddNewStall = { name, address, notes, photoUri, onCreated ->
                viewModel.addStall(name, address, notes, photoUri, onCreated)
            },
            onSave = { name, hpp, unit, defaultPrice, photoUri, stallPrices ->
                if (editingMenuItem != null) {
                    viewModel.updateMenuItemFull(
                        item = editingMenuItem!!.copy(
                            name = name,
                            hpp = hpp,
                            price = defaultPrice,
                            unit = unit,
                            photoUri = photoUri
                        ),
                        stallPrices = stallPrices
                    )
                } else {
                    viewModel.addMenuItemFull(
                        name = name,
                        hpp = hpp,
                        unit = unit,
                        defaultPrice = defaultPrice,
                        photoUri = photoUri,
                        stallPrices = stallPrices
                    )
                }
                showAddEditMenuDialog = false
            }
        )
    }

    // Dialog: Delete Menu Confirmation
    menuItemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { menuItemToDelete = null },
            title = { Text(strings.hapusMenu) },
            text = { Text("${strings.konfirmasiHapusMenu}: ${item.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMenuItem(item.id)
                        menuItemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.hapus)
                }
            },
            dismissButton = {
                TextButton(onClick = { menuItemToDelete = null }) {
                    Text(strings.batal)
                }
            }
        )
    }

    // Dialog: Add / Edit Lapak
    if (showAddEditStallDialog) {
        AddEditStallFullDialog(
            stall = editingStall,
            activeMenus = activeMenus,
            allPrices = allPrices,
            strings = strings,
            onDismiss = { showAddEditStallDialog = false },
            onSave = { name, address, notes, photoUri, initialPrices ->
                if (editingStall != null) {
                    viewModel.updateStall(
                        editingStall!!.copy(
                            name = name,
                            address = address,
                            notes = notes,
                            photoUri = photoUri
                        )
                    )
                    // Also save prices
                    initialPrices.forEach { (menuId, p) ->
                        viewModel.setStallPrice(editingStall!!.id, menuId, p)
                    }
                } else {
                    viewModel.addStall(name, address, notes, photoUri) { newStallId ->
                        initialPrices.forEach { (menuId, p) ->
                            viewModel.setStallPrice(newStallId, menuId, p)
                        }
                    }
                }
                showAddEditStallDialog = false
            }
        )
    }

    // Dialog: Delete Stall Confirmation
    stallToDelete?.let { stall ->
        AlertDialog(
            onDismissRequest = { stallToDelete = null },
            title = { Text(strings.hapusLapak) },
            text = { Text("${strings.konfirmasiHapusLapak}: ${stall.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStall(stall.id)
                        stallToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.hapus)
                }
            },
            dismissButton = {
                TextButton(onClick = { stallToDelete = null }) {
                    Text(strings.batal)
                }
            }
        )
    }
}

/**
 * Card component for displaying a Lapak with its full product price list.
 * Allows sellers to view and directly customize the selling price of each product for this stall.
 */
@Composable
fun StallWithMenuPricesCard(
    stall: StallEntity,
    activeMenus: List<MenuItemEntity>,
    allPrices: List<StallMenuPriceEntity>,
    strings: com.example.model.AppStrings,
    onEditStall: () -> Unit,
    onDeleteStall: () -> Unit,
    onToggleActive: () -> Unit,
    onSavePrice: (menuId: Long, newPrice: Double) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stall_full_card_${stall.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Stall Header: Photo/Avatar, Name, Address, Active toggle, Edit, Delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!stall.photoUri.isNullOrBlank() && File(stall.photoUri).exists()) {
                    AsyncImage(
                        model = File(stall.photoUri),
                        contentDescription = stall.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (stall.isActive) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = if (stall.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stall.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = if (stall.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                    )
                    if (stall.address.isNotBlank()) {
                        Text(
                            text = stall.address,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = if (stall.isActive) strings.aktif else strings.nonaktif,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (stall.isActive) StatusGreen else MaterialTheme.colorScheme.outline
                    )
                }

                // Active toggle switch
                Switch(
                    checked = stall.isActive,
                    onCheckedChange = { onToggleActive() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.padding(end = 4.dp)
                )

                // Edit stall button
                IconButton(
                    onClick = onEditStall,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = strings.editLapak,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete stall button
                IconButton(
                    onClick = onDeleteStall,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = strings.hapusLapak,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-Section: Harga Produk di Lapak Ini
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Harga Produk di ${stall.name}:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Otomatis digunakan saat setoran",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (activeMenus.isEmpty()) {
                    Text(
                        text = "Belum ada menu makanan aktif. Tambahkan menu di tab Daftar Menu terlebih dahulu.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    activeMenus.forEach { menu ->
                        val savedPriceEntity = allPrices.find { it.stallId == stall.id && it.menuId == menu.id }
                        val currentPrice = savedPriceEntity?.price ?: menu.price

                        ProductPriceRowInStall(
                            menu = menu,
                            savedPrice = currentPrice,
                            onSave = { newPrice ->
                                onSavePrice(menu.id, newPrice)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual product price row inside a Stall card with instant formatted editing and saving.
 */
@Composable
fun ProductPriceRowInStall(
    menu: MenuItemEntity,
    savedPrice: Double,
    onSave: (Double) -> Unit
) {
    var priceDigits by remember(savedPrice) {
        mutableStateOf(savedPrice.toLong().toString())
    }
    var isDirty by remember(savedPrice) { mutableStateOf(false) }

    val currentAmount = priceDigits.toDoubleOrNull() ?: 0.0
    val margin = (currentAmount - menu.hpp).coerceAtLeast(0.0)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = menu.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "HPP: ${FormatUtils.formatRupiah(menu.hpp)} | Laba: ${FormatUtils.formatRupiah(margin)}",
                fontSize = 10.sp,
                color = if (margin > 0) StatusGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Price Input
        OutlinedTextField(
            value = if (priceDigits.isEmpty()) "" else FormatUtils.formatNumber(currentAmount),
            onValueChange = { input ->
                val digits = input.filter { it.isDigit() }
                priceDigits = digits
                isDirty = true
            },
            prefix = { Text("Rp ", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            placeholder = { Text("0", fontSize = 11.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
                .width(130.dp)
                .height(48.dp)
                .testTag("input_stall_price_${menu.id}")
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Save Price Button
        Button(
            onClick = {
                val p = priceDigits.toDoubleOrNull() ?: 0.0
                if (p > 0) {
                    onSave(p)
                    isDirty = false
                }
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDirty) MaterialTheme.colorScheme.primary else StatusGreen
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier
                .height(38.dp)
                .testTag("btn_save_price_${menu.id}")
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Simpan",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Dialog for Adding / Editing a Stall along with initial prices for all active menus.
 */
@Composable
fun AddEditStallFullDialog(
    stall: StallEntity?,
    activeMenus: List<MenuItemEntity>,
    allPrices: List<StallMenuPriceEntity>,
    strings: com.example.model.AppStrings,
    onDismiss: () -> Unit,
    onSave: (name: String, address: String, notes: String, photoUri: String?, initialPrices: Map<Long, Double>) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(stall?.name ?: "") }
    var address by remember { mutableStateOf(stall?.address ?: "") }
    var notes by remember { mutableStateOf(stall?.notes ?: "") }
    var stallPhotoUri by remember { mutableStateOf(stall?.photoUri) }

    // Map of menuId -> price digits
    val priceMap = remember { mutableStateMapOf<Long, String>() }

    LaunchedEffect(stall, activeMenus) {
        activeMenus.forEach { m ->
            val p = if (stall != null) {
                allPrices.find { it.stallId == stall.id && it.menuId == m.id }?.price ?: m.price
            } else {
                m.price
            }
            if (!priceMap.containsKey(m.id)) {
                priceMap[m.id] = p.toLong().toString()
            }
        }
    }

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val saved = ImageStorageHelper.saveImageLocally(context, uri, "stall")
            if (saved != null) stallPhotoUri = saved
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (stall == null) "Tambah Lapak Baru" else "Edit Lapak",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Photo Section
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        if (!stallPhotoUri.isNullOrBlank() && File(stallPhotoUri!!).exists()) {
                            AsyncImage(
                                model = File(stallPhotoUri!!),
                                contentDescription = "Foto Lapak",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                            )
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Foto Lapak (Opsional)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        pickPhotoLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (stallPhotoUri == null) "Pilih Foto" else "Ganti", fontSize = 12.sp)
                                }

                                if (stallPhotoUri != null) {
                                    OutlinedButton(
                                        onClick = { stallPhotoUri = null },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Hapus", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                // Nama Lapak
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.namaLapakLabel) },
                    placeholder = { Text("Contoh: Tamba Rejo, Tugu Gajah") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_stall_name_field")
                )

                // Alamat Lapak
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(strings.alamatLapak) },
                    placeholder = { Text("Lokasi / Jalan lapak...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_stall_address_field")
                )

                // Catatan
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Lapak (Opsional)") },
                    placeholder = { Text("Nama penjaga, jam buka, dll...") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Setting harga produk untuk lapak ini
                if (activeMenus.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Atur Harga Jual Produk di Lapak Ini:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    activeMenus.forEach { menu ->
                        val currentDigits = priceMap[menu.id] ?: menu.price.toLong().toString()
                        val currentAmt = currentDigits.toDoubleOrNull() ?: menu.price
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = menu.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "Standar: ${FormatUtils.formatRupiah(menu.price)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            OutlinedTextField(
                                value = if (currentDigits.isEmpty()) "" else FormatUtils.formatNumber(currentAmt),
                                onValueChange = { str ->
                                    val digits = str.filter { it.isDigit() }
                                    priceMap[menu.id] = digits
                                },
                                prefix = { Text("Rp ", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.width(135.dp).height(48.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val parsedPrices = priceMap.mapValues {
                            it.value.toDoubleOrNull() ?: 10000.0
                        }
                        onSave(name.trim(), address.trim(), notes.trim(), stallPhotoUri, parsedPrices)
                    }
                },
                modifier = Modifier.testTag("dialog_stall_confirm_btn")
            ) {
                Text(strings.simpan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.batal)
            }
        }
    )
}

/**
 * Full Add/Edit Menu Dialog with direct Lapak prices configuration.
 */
@Composable
fun AddEditMenuFullDialog(
    item: MenuItemEntity?,
    stalls: List<StallEntity>,
    allPrices: List<StallMenuPriceEntity>,
    strings: com.example.model.AppStrings,
    onDismiss: () -> Unit,
    onUpdateStallNameAndPhoto: (stallId: Long, newName: String, newPhotoUri: String?) -> Unit,
    onAddNewStall: (name: String, address: String, notes: String, photoUri: String?, onCreated: (Long) -> Unit) -> Unit,
    onSave: (name: String, hpp: Double, unit: String, defaultPrice: Double, photoUri: String?, stallPrices: Map<Long, Double>) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(item?.name ?: "") }
    var unit by remember { mutableStateOf(item?.unit ?: "pcs") }
    var hppDigits by remember { mutableStateOf(item?.hpp?.toLong()?.toString() ?: "5000") }
    var defaultPriceDigits by remember { mutableStateOf(item?.price?.toLong()?.toString() ?: "10000") }
    var menuPhotoUri by remember { mutableStateOf(item?.photoUri) }

    val stallPriceMap = remember { mutableStateMapOf<Long, String>() }

    LaunchedEffect(item, stalls) {
        stalls.forEach { s ->
            val existingP = if (item != null) {
                allPrices.find { it.stallId == s.id && it.menuId == item.id }?.price ?: item.price
            } else {
                defaultPriceDigits.toDoubleOrNull() ?: 10000.0
            }
            if (!stallPriceMap.containsKey(s.id)) {
                stallPriceMap[s.id] = existingP.toLong().toString()
            }
        }
    }

    val pickMenuPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = ImageStorageHelper.saveImageLocally(context, uri, "menu")
            if (savedPath != null) {
                menuPhotoUri = savedPath
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (item == null) "Tambah Menu Baru" else "Edit Menu Makanan",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Photo Section
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        if (!menuPhotoUri.isNullOrBlank() && File(menuPhotoUri!!).exists()) {
                            AsyncImage(
                                model = File(menuPhotoUri!!),
                                contentDescription = "Foto Menu",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                            )
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Foto Menu Makanan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        pickMenuPhotoLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (menuPhotoUri == null) "Pilih Foto" else "Ganti", fontSize = 12.sp)
                                }

                                if (menuPhotoUri != null) {
                                    OutlinedButton(
                                        onClick = { menuPhotoUri = null },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Hapus", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                // Nama Makanan
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.namaMenu) },
                    placeholder = { Text("Contoh: Burger Daging, Risol Rogut") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_menu_name_input")
                )

                // Satuan
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text(strings.satuan) },
                    placeholder = { Text("pcs / porsi / bungkus / cup") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_menu_unit_input")
                )

                // HPP Modal
                val hppAmount = hppDigits.toDoubleOrNull() ?: 0.0
                OutlinedTextField(
                    value = if (hppDigits.isEmpty()) "" else FormatUtils.formatNumber(hppAmount),
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        hppDigits = digits
                    },
                    label = { Text("HPP (Modal per Porsi)") },
                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    placeholder = { Text("Contoh: 5.000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_menu_hpp_input")
                )

                // Harga Standar
                val defAmount = defaultPriceDigits.toDoubleOrNull() ?: 0.0
                OutlinedTextField(
                    value = if (defaultPriceDigits.isEmpty()) "" else FormatUtils.formatNumber(defAmount),
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        defaultPriceDigits = digits
                    },
                    label = { Text("Harga Jual Standar") },
                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    placeholder = { Text("Contoh: 10.000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_menu_default_price_input")
                )

                // Lapak & Harga Khusus
                if (stalls.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Harga Jual di Masing-masing Lapak:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    stalls.forEach { stall ->
                        val priceRaw = stallPriceMap[stall.id] ?: defaultPriceDigits
                        val priceAmount = priceRaw.toDoubleOrNull() ?: defAmount
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stall.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = if (priceRaw.isEmpty()) "" else FormatUtils.formatNumber(priceAmount),
                                onValueChange = { str ->
                                    val digits = str.filter { it.isDigit() }
                                    stallPriceMap[stall.id] = digits
                                },
                                prefix = { Text("Rp ", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.width(135.dp).height(48.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hpp = hppDigits.toDoubleOrNull() ?: 0.0
                    val defP = defaultPriceDigits.toDoubleOrNull() ?: 0.0
                    val convertedStallPrices = stallPriceMap.mapValues {
                        it.value.toDoubleOrNull() ?: defP
                    }
                    if (name.isNotBlank()) {
                        onSave(name.trim(), hpp, unit.trim(), defP, menuPhotoUri, convertedStallPrices)
                    }
                },
                modifier = Modifier.testTag("dialog_menu_save_button")
            ) {
                Text(strings.simpan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.batal)
            }
        }
    )
}
