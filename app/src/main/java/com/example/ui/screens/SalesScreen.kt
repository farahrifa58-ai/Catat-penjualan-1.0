package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.DepositItemEntity
import com.example.model.DepositWithDetails
import com.example.ui.components.ConditionStatusChip
import com.example.ui.components.PaymentStatusChip
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination
import com.example.util.FormatUtils

data class EditableSaleItem(
    val entity: DepositItemEntity,
    var sold: Int,
    var resaleable: Int,
    var damaged: Int,
    var selfConsumed: Int,
    var isExpanded: Boolean = false
) {
    val remaining: Int
        get() = (entity.quantitySent - sold).coerceAtLeast(0)

    val conditionSum: Int
        get() = resaleable + damaged + selfConsumed

    val isComplete: Boolean
        get() = conditionSum == remaining

    val subtotal: Double
        get() = sold * entity.priceSnapshot
}

@Composable
fun SalesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val strings = viewModel.strings
    val allDeposits by viewModel.allDeposits.collectAsStateWithLifecycle()
    val preselectedDeposit by viewModel.selectedDepositForSales.collectAsStateWithLifecycle()

    var activeDeposit by remember { mutableStateOf<DepositWithDetails?>(null) }
    var dropdownOpen by remember { mutableStateOf(false) }
    var directInputItem by remember { mutableStateOf<Pair<Int, EditableSaleItem>?>(null) }

    val editableItems = remember { mutableStateListOf<EditableSaleItem>() }

    // Sync activeDeposit when preselectedDeposit changes or default to first available deposit
    LaunchedEffect(preselectedDeposit, allDeposits) {
        val target = preselectedDeposit ?: activeDeposit ?: allDeposits.firstOrNull()
        activeDeposit = target
        editableItems.clear()
        target?.items?.forEach { item ->
            val initialSold = if (item.hasRecordedSale) item.quantitySold else 0
            val rem = (item.quantitySent - initialSold).coerceAtLeast(0)
            val initialResale = if (item.hasRecordedSale) item.resaleableQty else rem
            val initialDamaged = if (item.hasRecordedSale) item.damagedQty else 0
            val initialSelf = if (item.hasRecordedSale) item.selfConsumedQty else 0

            editableItems.add(
                EditableSaleItem(
                    entity = item,
                    sold = initialSold,
                    resaleable = initialResale,
                    damaged = initialDamaged,
                    selfConsumed = initialSelf,
                    isExpanded = rem > 0
                )
            )
        }
    }

    val totalCalculatedBill = editableItems.sumOf { it.subtotal }
    val totalSoldItems = editableItems.sumOf { it.sold }
    val totalRemainingItems = editableItems.sumOf { it.remaining }
    val allConditionsComplete = editableItems.all { it.isComplete }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Deposit Selector Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.inputHasilPenjualan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = strings.tagihanDihitungDariTerjual,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { dropdownOpen = true }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .testTag("sales_deposit_selector")
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (activeDeposit != null) {
                                        val name = activeDeposit?.deposit?.stallName
                                        if (!name.isNullOrBlank()) name else "Setoran Lapak"
                                    } else strings.pilihSetoranUntukPenjualan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                activeDeposit?.let {
                                    Text(
                                        text = "${FormatUtils.formatDateDisplay(it.deposit.date)} • ${it.totalSent} item dikirim",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }

                        DropdownMenu(
                            expanded = dropdownOpen,
                            onDismissRequest = { dropdownOpen = false }
                        ) {
                            allDeposits.forEach { dep ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = if (dep.deposit.stallName.isNotBlank()) dep.deposit.stallName else "Setoran",
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${FormatUtils.formatDateDisplay(dep.deposit.date)} (${dep.totalSent} item)",
                                                fontSize = 12.sp
                                            )
                                        }
                                    },
                                    onClick = {
                                        activeDeposit = dep
                                        viewModel.selectDepositForSales(dep)
                                        dropdownOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (activeDeposit == null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    ) {
                        Text(
                            text = strings.belumAdaSetoranHariIni,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.navigateTo(ScreenDestination.SETORAN) }
                        ) {
                            Text(strings.catatSetoran)
                        }
                    }
                }
            }
        } else {
            // 2. Info Banner: Formula reminder
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = strings.sisaOtomatis,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 3. List of Items to Record Sales
            items(editableItems, key = { "sale_item_${it.entity.id}" }) { item ->
                val idx = editableItems.indexOf(item)

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("sales_item_card_${item.entity.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.entity.menuName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${FormatUtils.formatRupiah(item.entity.priceSnapshot)} / ${item.entity.unit}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            ConditionStatusChip(isComplete = item.isComplete, strings = strings)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Sent, Sold, Remaining Controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Dikirim
                            Column {
                                Text(text = strings.dikirim, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "${item.entity.quantitySent}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }

                            // Terjual Stepper
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = strings.terjual, fontSize = 11.sp, color = StatusGreen, fontWeight = FontWeight.SemiBold)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(StatusGreen.copy(alpha = 0.1f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (item.sold > 0 && idx >= 0) {
                                                val newSold = item.sold - 1
                                                val newRem = item.entity.quantitySent - newSold
                                                editableItems[idx] = item.copy(
                                                    sold = newSold,
                                                    resaleable = newRem,
                                                    damaged = 0,
                                                    selfConsumed = 0
                                                )
                                            }
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(16.dp))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { directInputItem = idx to item }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${item.sold}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = StatusGreen
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (item.sold < item.entity.quantitySent && idx >= 0) {
                                                val newSold = item.sold + 1
                                                val newRem = item.entity.quantitySent - newSold
                                                editableItems[idx] = item.copy(
                                                    sold = newSold,
                                                    resaleable = newRem,
                                                    damaged = 0,
                                                    selfConsumed = 0
                                                )
                                            }
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            // Sisa
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = strings.sisa, fontSize = 11.sp, color = StatusAmber, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "${item.remaining}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusAmber
                                )
                            }
                        }

                        // Subtotal Tagihan
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Tagihan (${item.sold} × ${FormatUtils.formatRupiah(item.entity.priceSnapshot)})",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = FormatUtils.formatRupiah(item.subtotal),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Remaining condition allocation section (Section 6)
                        if (item.remaining > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "${strings.kondisiSisaMakanan} (Total Sisa: ${item.remaining})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Quick allocation helpers
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        if (idx >= 0) {
                                            editableItems[idx] = item.copy(
                                                resaleable = item.remaining,
                                                damaged = 0,
                                                selfConsumed = 0
                                            )
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(text = "Semua Layak", fontSize = 10.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        if (idx >= 0) {
                                            editableItems[idx] = item.copy(
                                                resaleable = 0,
                                                damaged = item.remaining,
                                                selfConsumed = 0
                                            )
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(text = "Semua Rusak", fontSize = 10.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        if (idx >= 0) {
                                            editableItems[idx] = item.copy(
                                                resaleable = 0,
                                                damaged = 0,
                                                selfConsumed = item.remaining
                                            )
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(text = "Semua Konsumsi", fontSize = 10.sp)
                                }
                            }

                            // 3 condition rows: Layak Jual, Rusak, Konsumsi Sendiri
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(8.dp)
                            ) {
                                ConditionRow(
                                    label = strings.layakJual,
                                    qty = item.resaleable,
                                    max = item.remaining,
                                    onIncrease = {
                                        if (item.conditionSum < item.remaining && idx >= 0) {
                                            editableItems[idx] = item.copy(resaleable = item.resaleable + 1)
                                        }
                                    },
                                    onDecrease = {
                                        if (item.resaleable > 0 && idx >= 0) {
                                            editableItems[idx] = item.copy(resaleable = item.resaleable - 1)
                                        }
                                    }
                                )
                                ConditionRow(
                                    label = strings.rusakTidakLayak,
                                    qty = item.damaged,
                                    max = item.remaining,
                                    onIncrease = {
                                        if (item.conditionSum < item.remaining && idx >= 0) {
                                            editableItems[idx] = item.copy(damaged = item.damaged + 1)
                                        }
                                    },
                                    onDecrease = {
                                        if (item.damaged > 0 && idx >= 0) {
                                            editableItems[idx] = item.copy(damaged = item.damaged - 1)
                                        }
                                    }
                                )
                                ConditionRow(
                                    label = strings.konsumsiSendiri,
                                    qty = item.selfConsumed,
                                    max = item.remaining,
                                    onIncrease = {
                                        if (item.conditionSum < item.remaining && idx >= 0) {
                                            editableItems[idx] = item.copy(selfConsumed = item.selfConsumed + 1)
                                        }
                                    },
                                    onDecrease = {
                                        if (item.selfConsumed > 0 && idx >= 0) {
                                            editableItems[idx] = item.copy(selfConsumed = item.selfConsumed - 1)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 4. Live Total Bill Summary Card (Section 7)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = strings.totalTagihanLapak,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = FormatUtils.formatRupiah(totalCalculatedBill),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Terjual: $totalSoldItems makanan | Sisa: $totalRemainingItems makanan",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        if (!allConditionsComplete) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Peringatan: Kategori kondisi sisa makanan belum lengkap.",
                                fontSize = 11.sp,
                                color = StatusAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 5. Save Sales Results Button
            item {
                Button(
                    onClick = {
                        val updatedEntities = editableItems.map {
                            it.entity.copy(
                                quantitySold = it.sold,
                                hasRecordedSale = true,
                                resaleableQty = it.resaleable,
                                damagedQty = it.damaged,
                                selfConsumedQty = it.selfConsumed
                            )
                        }
                        activeDeposit?.let { dep ->
                            viewModel.saveSalesResults(
                                items = updatedEntities,
                                onSuccess = {
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(0)
                                    }
                                }
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("sales_save_button")
                ) {
                    Text(
                        text = strings.simpanHasilPenjualan,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    val directItem = directInputItem
    if (directItem != null) {
        val (idx, item) = directItem
        var inputStr by remember(directItem) { mutableStateOf("${item.sold}") }
        AlertDialog(
            onDismissRequest = { directInputItem = null },
            title = { Text("Jumlah Terjual: ${item.entity.menuName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Total Dikirim: ${item.entity.quantitySent} ${item.entity.unit}")
                    OutlinedTextField(
                        value = inputStr,
                        onValueChange = { inputStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Jumlah Terjual (0 - ${item.entity.quantitySent})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val typed = (inputStr.toIntOrNull() ?: 0).coerceIn(0, item.entity.quantitySent)
                        val newRem = item.entity.quantitySent - typed
                        editableItems[idx] = item.copy(
                            sold = typed,
                            resaleable = newRem,
                            damaged = 0,
                            selfConsumed = 0
                        )
                        directInputItem = null
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { directInputItem = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun ConditionRow(
    label: String,
    qty: Int,
    max: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onDecrease,
                enabled = qty > 0,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(14.dp))
            }
            Text(
                text = "$qty",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            IconButton(
                onClick = onIncrease,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(14.dp))
            }
        }
    }
}
