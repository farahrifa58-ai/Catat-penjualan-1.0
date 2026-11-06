package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.style.TextOverflow
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
    var notes by remember { mutableStateOf("") }

    // Draft items list
    val draftItems = remember { mutableStateListOf<DraftDepositItem>() }

    // Map to keep track of drafted quantities per stallId -> (menuId -> qty)
    val stallQuantities = remember { mutableStateMapOf<Long, MutableMap<Long, Int>>() }

    fun reloadDraftItemsForStall(stall: StallEntity?) {
        if (stall == null) return
        draftItems.clear()
        val savedQtys = stallQuantities[stall.id] ?: emptyMap()
        activeMenus.forEachIndexed { index, m ->
            val specificPrice = allStallPrices.find { it.stallId == stall.id && it.menuId == m.id }?.price ?: m.price
            // Crucial: If user hasn't set anything for THIS stall, quantity starts strictly from 0!
            val initialQty = savedQtys[m.id] ?: 0
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

    // Set initial stall if none selected
    LaunchedEffect(activeStalls) {
        if (selectedStall == null && activeStalls.isNotEmpty()) {
            selectedStall = activeStalls.firstOrNull()
        }
    }

    // When selectedStall or activeMenus or prices change, reload draft items specifically for this stall!
    LaunchedEffect(selectedStall?.id, activeMenus, allStallPrices) {
        selectedStall?.let { stall ->
            reloadDraftItemsForStall(stall)
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
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() }
                            .testTag("deposit_form_date")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // === HORIZONTAL STALL SELECTOR (DAFTAR LAPAK KE SAMPING) ===
                    Text(
                        text = "Lapak Tujuan (Pilih Lapak):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (activeStalls.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(12.dp)
                            ) {
                                Text(
                                    text = "Belum ada lapak terdaftar.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = { viewModel.navigateTo(ScreenDestination.MENU) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("+ Tambah Lapak", fontSize = 11.sp)
                                }
                            }
                        }
                    } else {
                        // Horizontal scrollable row of stall cards
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp)
                        ) {
                            activeStalls.forEach { stall ->
                                val isSelected = selectedStall?.id == stall.id
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedStall = stall
                                            reloadDraftItemsForStall(stall)
                                        }
                                        .testTag("stall_select_${stall.id}")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                                )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Storefront,
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = stall.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (isSelected) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            if (stall.address.isNotBlank()) {
                                                Text(
                                                    text = stall.address,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Additional Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(strings.catatanTambahan) },
                        placeholder = { Text(strings.catatanHint) },
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp),
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
                shape = RoundedCornerShape(14.dp),
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

        // List of items in draft (Redesigned: Clean, spacious, no squeezed price text!)
        items(draftItems, key = { "draft_${it.tempId}" }) { item ->
            val idx = draftItems.indexOf(item)

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("draft_item_${item.tempId}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Top row: Avatar, Food Name, and Delete Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = item.menuName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = {
                                selectedStall?.let { s ->
                                    item.menuId?.let { mId ->
                                        val qMap = stallQuantities.getOrPut(s.id) { mutableMapOf() }
                                        qMap[mId] = 0
                                    }
                                }
                                draftItems.remove(item)
                            },
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bottom row: Price Badge (Left) and Stepper (Right)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Clean price badge with weight so it never pushes the stepper
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Harga: ${FormatUtils.formatRupiah(item.priceSnapshot)} / ${item.unit}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Stepper: - [Qty] + (Fixed and always fully visible with clear contrast)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(horizontal = 2.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (idx >= 0 && item.quantitySent > 0) {
                                        val newQ = item.quantitySent - 1
                                        draftItems[idx] = item.copy(quantitySent = newQ)
                                        selectedStall?.let { s ->
                                            item.menuId?.let { mId ->
                                                val qMap = stallQuantities.getOrPut(s.id) { mutableMapOf() }
                                                qMap[mId] = newQ
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(16.dp))
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "${item.quantitySent}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (item.quantitySent > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (idx >= 0) {
                                        val newQ = item.quantitySent + 1
                                        draftItems[idx] = item.copy(quantitySent = newQ)
                                        selectedStall?.let { s ->
                                            item.menuId?.let { mId ->
                                                val qMap = stallQuantities.getOrPut(s.id) { mutableMapOf() }
                                                qMap[mId] = newQ
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(16.dp))
                            }
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
                                    val newQ = existing.quantitySent + 1
                                    draftItems[eIdx] = existing.copy(quantitySent = newQ)
                                    selectedStall?.let { s ->
                                        val qMap = stallQuantities.getOrPut(s.id) { mutableMapOf() }
                                        qMap[menu.id] = newQ
                                    }
                                } else {
                                    val initialQ = 1
                                    draftItems.add(
                                        DraftDepositItem(
                                            tempId = System.currentTimeMillis() + draftItems.size,
                                            menuId = menu.id,
                                            menuName = menu.name,
                                            priceSnapshot = stallPrice,
                                            unit = menu.unit,
                                            quantitySent = initialQ
                                        )
                                    )
                                    selectedStall?.let { s ->
                                        val qMap = stallQuantities.getOrPut(s.id) { mutableMapOf() }
                                        qMap[menu.id] = initialQ
                                    }
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
                            selectedStall?.let { s ->
                                stallQuantities[s.id]?.clear()
                                reloadDraftItemsForStall(s)
                            }
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
