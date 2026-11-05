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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
    val stalls by viewModel.activeStalls.collectAsStateWithLifecycle()
    val allPrices by viewModel.allStallPrices.collectAsStateWithLifecycle()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<MenuItemEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<MenuItemEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingItem = null
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("menu_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.tambahMenu)
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Notice Card
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
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
                            text = "Setiap menu dapat ditambahkan foto, HPP (biaya modal), serta harga jual khusus untuk masing-masing lapak.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
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
                                text = "Silakan klik tombol '+' di bawah untuk menambahkan menu pertama Anda.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    editingItem = null
                                    showAddEditDialog = true
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.tambahMenu)
                            }
                        }
                    }
                }
            } else {
                items(menuItems, key = { "menu_${it.id}" }) { item ->
                    val menuStallPrices = allPrices.filter { it.menuId == item.id }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("menu_item_${item.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Food Photo or Fallback Icon
                                if (!item.photoUri.isNullOrBlank() && File(item.photoUri).exists()) {
                                    AsyncImage(
                                        model = File(item.photoUri),
                                        contentDescription = item.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    )
                                } else {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (item.isActive) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Fastfood,
                                            contentDescription = null,
                                            tint = if (item.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (item.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = "Satuan: ${item.unit} • HPP: ${FormatUtils.formatRupiah(item.hpp)}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Active toggle switch
                                Switch(
                                    checked = item.isActive,
                                    onCheckedChange = { viewModel.toggleMenuItemActive(item) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )

                                // Edit button
                                IconButton(onClick = {
                                    editingItem = item
                                    showAddEditDialog = true
                                }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = strings.editMenu,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Delete button
                                IconButton(onClick = { itemToDelete = item }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = strings.hapusMenu,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            // Prices per stall breakdown section
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                if (menuStallPrices.isEmpty()) {
                                    Text(
                                        text = "Harga Standar: ${FormatUtils.formatRupiah(item.price)} / ${item.unit}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    val priceTexts = menuStallPrices.map { sp ->
                                        val sName = stalls.find { it.id == sp.stallId }?.name ?: "Lapak"
                                        "$sName: ${FormatUtils.formatRupiah(sp.price)}"
                                    }.joinToString("  •  ")

                                    Text(
                                        text = priceTexts,
                                        fontSize = 11.sp,
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

    // Add / Edit Menu Dialog with Photo, HPP, Edit Lapak, & Stall Prices
    if (showAddEditDialog) {
        AddEditMenuFullDialog(
            item = editingItem,
            stalls = stalls,
            allPrices = allPrices,
            strings = strings,
            onDismiss = { showAddEditDialog = false },
            onUpdateStallNameAndPhoto = { stallId, newName, newPhotoUri ->
                viewModel.updateStallNameAndPhoto(stallId, newName, newPhotoUri)
            },
            onAddNewStall = { name, address, notes, photoUri, onCreated ->
                viewModel.addStall(name, address, notes, photoUri, onCreated)
            },
            onSave = { name, hpp, unit, defaultPrice, photoUri, stallPrices ->
                if (editingItem != null) {
                    viewModel.updateMenuItemFull(
                        item = editingItem!!.copy(
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
                showAddEditDialog = false
            }
        )
    }

    // Delete confirmation dialog
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(strings.hapusMenu) },
            text = { Text("${strings.konfirmasiHapusMenu}: ${item.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMenuItem(item.id)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.hapus)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(strings.batal)
                }
            }
        )
    }
}

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

    // Sub-dialog states for managing stalls right from here!
    var editingStallTarget by remember { mutableStateOf<StallEntity?>(null) }
    var showCreateStallDialog by remember { mutableStateOf(false) }

    // Map of stallId -> digits string
    val stallPriceMap = remember { mutableStateMapOf<Long, String>() }

    LaunchedEffect(item, stalls) {
        stalls.forEach { s ->
            val existingP = if (item != null) {
                allPrices.find { it.stallId == s.id && it.menuId == item.id }?.price ?: item.price
            } else {
                10000.0
            }
            if (!stallPriceMap.containsKey(s.id)) {
                stallPriceMap[s.id] = existingP.toLong().toString()
            }
        }
    }

    // Photo picker for menu photo (Zero permissions via Android Photo Picker)
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
                // 1. Upload Foto Menu Section
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

                // 2. Nama Makanan
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.namaMenu) },
                    placeholder = { Text("Contoh: Burger Daging, Risol Rogut") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_menu_name_input")
                )

                // 3. Satuan
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text(strings.satuan) },
                    placeholder = { Text("pcs / porsi / bungkus / cup") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_menu_unit_input")
                )

                // 4. HPP (Harga Pokok Produksi / Biaya Modal) - dengan format titik
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

                // 5. Harga Jual Standar (Default)
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

                // 6. Section: Lapak & Harga Khusus
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lapak & Harga Khusus",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (stalls.isEmpty()) "Belum ada lapak terdaftar:" else "Atur harga khusus untuk lapak:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Button: Tambah Lapak Baru langsung dari sini!
                    Button(
                        onClick = { showCreateStallDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp).testTag("dialog_btn_add_stall")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Tambah Lapak", fontSize = 11.sp)
                    }
                }

                if (stalls.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(14.dp)
                        ) {
                            Text(
                                text = "Belum ada lapak. Klik tombol '+ Tambah Lapak' di atas untuk membuat lapak baru.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    stalls.forEach { stall ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                // Stall photo or icon
                                if (!stall.photoUri.isNullOrBlank() && File(stall.photoUri).exists()) {
                                    AsyncImage(
                                        model = File(stall.photoUri),
                                        contentDescription = stall.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Storefront,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Stall Name + Edit Button
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = stall.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        IconButton(
                                            onClick = { editingStallTarget = stall },
                                            modifier = Modifier.size(28.dp).testTag("edit_stall_${stall.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Nama Lapak",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                    if (stall.address.isNotBlank()) {
                                        Text(
                                            text = stall.address,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Price text field with dot thousand separator
                                val priceRaw = stallPriceMap[stall.id] ?: ""
                                val priceAmount = priceRaw.toDoubleOrNull() ?: 0.0
                                OutlinedTextField(
                                    value = if (priceRaw.isEmpty()) "" else FormatUtils.formatNumber(priceAmount),
                                    onValueChange = { str ->
                                        val digits = str.filter { it.isDigit() }
                                        stallPriceMap[stall.id] = digits
                                    },
                                    prefix = { Text("Rp ", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    placeholder = { Text("0", fontSize = 11.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.width(135.dp).testTag("dialog_price_stall_${stall.id}")
                                )
                            }
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

    // Sub-dialog: Edit Nama Lapak & Foto Lapak
    editingStallTarget?.let { targetStall ->
        EditStallDialog(
            stall = targetStall,
            onDismiss = { editingStallTarget = null },
            onSave = { newName, newPhotoUri ->
                onUpdateStallNameAndPhoto(targetStall.id, newName, newPhotoUri)
                editingStallTarget = null
            }
        )
    }

    // Sub-dialog: Tambah Lapak Baru
    if (showCreateStallDialog) {
        CreateStallDialog(
            onDismiss = { showCreateStallDialog = false },
            onSave = { stallName, stallAddress, stallNotes, photoUri ->
                onAddNewStall(stallName, stallAddress, stallNotes, photoUri) { newStallId ->
                    // Set default price for new stall
                    val defP = defaultPriceDigits.toDoubleOrNull() ?: 10000.0
                    stallPriceMap[newStallId] = defP.toLong().toString()
                }
                showCreateStallDialog = false
            }
        )
    }
}

@Composable
fun EditStallDialog(
    stall: StallEntity,
    onDismiss: () -> Unit,
    onSave: (newName: String, newPhotoUri: String?) -> Unit
) {
    val context = LocalContext.current
    var stallName by remember { mutableStateOf(stall.name) }
    var stallPhotoUri by remember { mutableStateOf(stall.photoUri) }

    val pickStallPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = ImageStorageHelper.saveImageLocally(context, uri, "stall")
            if (savedPath != null) {
                stallPhotoUri = savedPath
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Nama & Foto Lapak", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Foto Lapak Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!stallPhotoUri.isNullOrBlank() && File(stallPhotoUri!!).exists()) {
                        AsyncImage(
                            model = File(stallPhotoUri!!),
                            contentDescription = "Foto Lapak",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Button(
                            onClick = {
                                pickStallPhotoLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (stallPhotoUri == null) "Foto Lapak" else "Ganti Foto", fontSize = 12.sp)
                        }

                        if (stallPhotoUri != null) {
                            TextButton(
                                onClick = { stallPhotoUri = null },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("Hapus Foto", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = stallName,
                    onValueChange = { stallName = it },
                    label = { Text("Nama Lapak") },
                    placeholder = { Text("Contoh: Lapak A, Lapak Simpang 5") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (stallName.isNotBlank()) {
                        onSave(stallName.trim(), stallPhotoUri)
                    }
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun CreateStallDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, address: String, notes: String, photoUri: String?) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = ImageStorageHelper.saveImageLocally(context, uri, "stall")
            if (savedPath != null) {
                photoUri = savedPath
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Lapak Baru", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!photoUri.isNullOrBlank() && File(photoUri!!).exists()) {
                        AsyncImage(
                            model = File(photoUri!!),
                            contentDescription = "Foto Lapak",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

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
                        Text(if (photoUri == null) "+ Foto Lapak" else "Ganti Foto", fontSize = 11.sp)
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lapak") },
                    placeholder = { Text("Contoh: Lapak D, Lapak Pasar Baru") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Alamat / Lokasi (Opsional)") },
                    placeholder = { Text("Contoh: Blok B No. 12") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), address.trim(), notes.trim(), photoUri)
                    }
                }
            ) {
                Text("Tambah")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
