package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.DepositItemEntity
import com.example.data.local.entity.StallEntity
import com.example.ui.components.PaymentStatusChip
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination
import com.example.util.FormatUtils
import java.util.Calendar

data class DraftDepositItem(
    val tempId: Long,
    val menuId: Long?,
    var menuName: String,
    var priceSnapshot: Double,
    var unit: String,
    var quantitySent: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = viewModel.strings

    val activeMenus by viewModel.activeMenuItems.collectAsStateWithLifecycle()
    val activeStalls by viewModel.activeStalls.collectAsStateWithLifecycle()
    val allStallPrices by viewModel.allStallPrices.collectAsStateWithLifecycle()
    val allDeposits by viewModel.allDeposits.collectAsStateWithLifecycle()

    var dateText by remember { mutableStateOf(FormatUtils.getTodayDateIso()) }
    var selectedStall by remember { mutableStateOf<StallEntity?>(null) }
    var stallDropdownOpen by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    // Draft items list
    val draftItems = remember { mutableStateListOf<DraftDepositItem>() }

    // Set initial stall if none selected
    LaunchedEffect(activeStalls) {
        if (selectedStall == null && activeStalls.isNotEmpty()) {
            selectedStall = activeStalls.firstOrNull()
        }
    }

    // When selectedStall changes, refresh draft items with stall-specific pricing!
    LaunchedEffect(selectedStall, activeMenus, allStallPrices) {
        val stall = selectedStall
        if (stall != null && draftItems.isEmpty() && activeMenus.isNotEmpty()) {
            activeMenus.forEachIndexed { index, m ->
                val specificPrice = allStallPrices.find { it.stallId == stall.id && it.menuId == m.id }?.price ?: m.price
                val initialQty = when (m.name.lowercase()) {
                    "burger" -> 20
                    "sandwich" -> 15
                    "spaghetti" -> 20
                    else -> 0
                }
                draftItems.add(
                    DraftDepositItem(
                        tempId = index.toLong(),
                        menuId = m.id,
                        menuName = m.name,
                        priceSnapshot = specificPrice,
                        unit = m.unit,
                        quantitySent = initialQty
                    )
                )
            }
        }
    }

    // Date picker
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            dateText = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val totalFoodCount = draftItems.sumOf { it.quantitySent }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Form Header Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.catatSetoran,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = strings.formSetoranSubtitle,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Date Input
                    OutlinedTextField(
                        value = FormatUtils.formatDateDisplay(dateText),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.tanggalSetoran) },
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() }
                            .testTag("deposit_form_date")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stall Selector Dropdown (Mandatory per Section C)
                    Text(text = strings.namaLapak, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { stallDropdownOpen = true }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .testTag("deposit_form_stall_picker")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = selectedStall?.name ?: strings.namaLapakHint,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (selectedStall != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                                    )
                                    selectedStall?.let {
                                        if (it.address.isNotBlank()) {
                                            Text(text = it.address, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = stallDropdownOpen,
                            onDismissRequest = { stallDropdownOpen = false }
                        ) {
                            if (activeStalls.isEmpty()) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Belum ada lapak. Tambah lapak di Menu",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    onClick = {
                                        stallDropdownOpen = false
                                        viewModel.navigateTo(ScreenDestination.MENU)
                                    }
                                )
                            } else {
                                activeStalls.forEach { stall ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(text = stall.name, fontWeight = FontWeight.Bold)
                                                if (stall.address.isNotBlank()) {
                                                    Text(text = stall.address, fontSize = 11.sp)
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedStall = stall
                                            stallDropdownOpen = false
                                            // Update price snapshots for draft items to this stall's prices!
                                            draftItems.forEachIndexed { i, item ->
                                                if (item.menuId != null) {
                                                    val sp = allStallPrices.find { it.stallId == stall.id && it.menuId == item.menuId }?.price
                                                    if (sp != null) {
                                                        draftItems[i] = item.copy(priceSnapshot = sp)
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Additional Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(strings.catatanTambahan) },
                        placeholder = { Text(strings.catatanHint) },
                        maxLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deposit_form_notes")
                    )
                }
            }
        }

        // Section: Total Makanan Dikirim (Card Banner yang rapi & jelas)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fastfood,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = strings.totalMakananDikirim,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${draftItems.size} jenis makanan dipilih",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$totalFoodCount Item",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // List of items in draft
        items(draftItems, key = { "draft_${it.tempId}" }) { item ->
            val idx = draftItems.indexOf(item)

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fastfood,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.menuName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Harga Lapak: ${FormatUtils.formatRupiah(item.priceSnapshot)} / ${item.unit}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Stepper: - [Qty] +
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (idx >= 0 && item.quantitySent > 0) {
                                        draftItems[idx] = item.copy(quantitySent = item.quantitySent - 1)
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(16.dp))
                            }

                            Text(
                                text = "${item.quantitySent}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            IconButton(
                                onClick = {
                                    if (idx >= 0) {
                                        draftItems[idx] = item.copy(quantitySent = item.quantitySent + 1)
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = { draftItems.remove(item) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Hapus Menu",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Price snapshot editable field if price needs manual adjustment or was not set
                    if (item.priceSnapshot <= 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Masukkan harga lapak:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = item.priceSnapshot.toLong().toString(),
                                onValueChange = { str ->
                                    val newP = str.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
                                    draftItems[idx] = item.copy(priceSnapshot = newP)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.width(110.dp).height(46.dp)
                            )
                        }
                    }
                }
            }
        }

        // Add menu button
        item {
            var menuExpanded by remember { mutableStateOf(false) }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { menuExpanded = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deposit_add_item_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.tambahMenuSetoran)
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    activeMenus.forEach { menu ->
                        val stallPrice = selectedStall?.let { s ->
                            allStallPrices.find { it.stallId == s.id && it.menuId == menu.id }?.price
                        } ?: menu.price

                        DropdownMenuItem(
                            text = {
                                Text("${menu.name} - ${FormatUtils.formatRupiah(stallPrice)} / ${menu.unit}")
                            },
                            onClick = {
                                val existing = draftItems.find { it.menuId == menu.id }
                                if (existing != null) {
                                    val eIdx = draftItems.indexOf(existing)
                                    draftItems[eIdx] = existing.copy(quantitySent = existing.quantitySent + 5)
                                } else {
                                    draftItems.add(
                                        DraftDepositItem(
                                            tempId = System.currentTimeMillis() + draftItems.size,
                                            menuId = menu.id,
                                            menuName = menu.name,
                                            priceSnapshot = stallPrice,
                                            unit = menu.unit,
                                            quantitySent = 10
                                        )
                                    )
                                }
                                menuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Save Setoran Button
        item {
            Button(
                onClick = {
                    val currentStall = selectedStall
                    val depositItems = draftItems.map {
                        DepositItemEntity(
                            depositId = 0,
                            menuId = it.menuId,
                            menuName = it.menuName,
                            priceSnapshot = it.priceSnapshot,
                            unit = it.unit,
                            quantitySent = it.quantitySent,
                            quantitySold = 0,
                            hasRecordedSale = false,
                            resaleableQty = 0,
                            damagedQty = 0,
                            selfConsumedQty = 0
                        )
                    }
                    viewModel.createDeposit(
                        date = dateText,
                        stallId = currentStall?.id,
                        stallName = currentStall?.name ?: "",
                        notes = notes,
                        items = depositItems,
                        onSuccess = {
                            notes = ""
                        }
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("deposit_save_button")
            ) {
                Text(
                    text = strings.simpanSetoran,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        // Saved Deposits List Header
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = strings.listSetoranTitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // List of existing deposits
        if (allDeposits.isEmpty()) {
            item {
                Text(
                    text = strings.tidakAdaAktivitas,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(allDeposits.take(5), key = { "recent_dep_${it.deposit.id}" }) { dep ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectDepositForDetail(dep) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (dep.deposit.stallName.isNotBlank()) dep.deposit.stallName else "Setoran Lapak",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${FormatUtils.formatDateDisplay(dep.deposit.date)} • ${dep.totalSent} item dikirim",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        PaymentStatusChip(status = dep.paymentStatus, strings = strings)
                    }
                }
            }
        }
    }
}
