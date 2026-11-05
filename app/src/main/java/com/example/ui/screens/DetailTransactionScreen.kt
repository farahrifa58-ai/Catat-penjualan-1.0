package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.model.PaymentStatus
import com.example.ui.components.ConditionStatusChip
import com.example.ui.components.PaymentStatusChip
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination
import com.example.util.FormatUtils

@Composable
fun DetailTransactionScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = viewModel.strings
    val depositWithDetails by viewModel.selectedDepositForDetail.collectAsStateWithLifecycle()

    var showPaymentDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val deposit = depositWithDetails
    if (deposit == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tidak ada data transaksi dipilih.")
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Transaction Overview Header Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (deposit.deposit.stallName.isNotBlank()) deposit.deposit.stallName else "Setoran Lapak",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = FormatUtils.formatDateDisplay(deposit.deposit.date),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        PaymentStatusChip(status = deposit.paymentStatus, strings = strings)
                    }

                    if (deposit.deposit.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Catatan: ${deposit.deposit.notes}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats breakdown row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(text = strings.dikirim, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "${deposit.totalSent}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StatusGreen.copy(alpha = 0.1f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(text = strings.terjual, fontSize = 11.sp, color = StatusGreen)
                                Text(text = "${deposit.totalSold}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = StatusGreen)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StatusAmber.copy(alpha = 0.1f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(text = strings.sisa, fontSize = 11.sp, color = StatusAmber)
                                Text(text = "${deposit.totalRemaining}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = StatusAmber)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tagihan vs Pembayaran Box
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(text = strings.totalTagihanLapak, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = FormatUtils.formatRupiah(deposit.totalBill),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = strings.sisaTagihanLapak, fontSize = 12.sp, color = if (deposit.remainingBill > 0) StatusRed else StatusGreen)
                            Text(
                                text = FormatUtils.formatRupiah(deposit.remainingBill),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (deposit.remainingBill > 0) StatusRed else StatusGreen
                            )
                        }
                    }
                }
            }
        }

        // 2. Action Buttons (Record/Update Sales, Add Payment)
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        viewModel.selectDepositForSales(deposit)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f).height(44.dp).testTag("detail_edit_sales_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = strings.inputHasilPenjualan, fontSize = 12.sp)
                }

                Button(
                    onClick = { showPaymentDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.weight(1f).height(44.dp).testTag("detail_add_payment_button")
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = strings.pembayaranSusulan, fontSize = 12.sp)
                }
            }
        }

        // 3. Itemized Food Details Section (Section 8)
        item {
            Text(
                text = strings.rincianMenu,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(deposit.items, key = { "det_item_${it.id}" }) { item ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item.menuName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = FormatUtils.formatRupiah(item.subtotalBill),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Harga: ${FormatUtils.formatRupiah(item.priceSnapshot)} / ${item.unit} • Dikirim: ${item.quantitySent} • Terjual: ${item.quantitySold} • Sisa: ${item.remainingQty}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (item.remainingQty > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Kondisi: Layak (${item.resaleableQty}), Rusak (${item.damagedQty}), Konsumsi (${item.selfConsumedQty})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ConditionStatusChip(isComplete = item.isConditionComplete, strings = strings)
                        }
                    }
                }
            }
        }

        // 4. Payment History (Riwayat Pembayaran)
        item {
            Text(
                text = "${strings.riwayatPembayaran} (${deposit.payments.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (deposit.payments.isEmpty()) {
            item {
                Text(
                    text = "Belum ada pembayaran yang dicatat.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(deposit.payments, key = { "det_pay_${it.id}" }) { payment ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = FormatUtils.formatRupiah(payment.amountPaid),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = StatusGreen
                            )
                            Text(
                                text = "${payment.paymentDate} • Metode: ${payment.paymentMethod}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (payment.notes.isNotBlank()) {
                                Text(
                                    text = "Catatan: ${payment.notes}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.deletePayment(payment.id, deposit.deposit.id) }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Hapus Pembayaran",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. Delete Transaction Button
        item {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = { showDeleteConfirmDialog = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("detail_delete_deposit_button")
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.hapusTransaksi)
            }
        }
    }

    // Payment Dialog
    if (showPaymentDialog) {
        AddPaymentDialog(
            remainingBill = deposit.remainingBill,
            strings = strings,
            onDismiss = { showPaymentDialog = false },
            onConfirm = { amount, date, method, notes ->
                viewModel.recordPayment(
                    depositId = deposit.deposit.id,
                    amount = amount,
                    date = date,
                    method = method,
                    notes = notes,
                    onSuccess = { showPaymentDialog = false }
                )
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(strings.konfirmasiHapusTransaksi) },
            text = { Text(strings.konfirmasiHapusDesc) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteDeposit(deposit.deposit.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.hapus)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(strings.batal)
                }
            }
        )
    }
}

@Composable
fun AddPaymentDialog(
    remainingBill: Double,
    strings: com.example.model.AppStrings,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, date: String, method: String, notes: String) -> Unit
) {
    var amountText by remember { mutableStateOf(if (remainingBill > 0) remainingBill.toLong().toString() else "0") }
    var method by remember { mutableStateOf("Tunai") }
    var notes by remember { mutableStateOf("") }
    val paymentDate = remember { FormatUtils.getTodayDateIso() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.catatPembayaran) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Sisa Tagihan: ${FormatUtils.formatRupiah(remainingBill)}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                val parsedAmt = amountText.toDoubleOrNull() ?: 0.0
                OutlinedTextField(
                    value = if (amountText.isEmpty()) "" else FormatUtils.formatNumber(parsedAmt),
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text(strings.uangDiterima) },
                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                )

                Text(text = strings.metodePembayaran, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    listOf("Tunai", "Transfer", "QRIS").forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            RadioButton(
                                selected = method == item,
                                onClick = { method = item }
                            )
                            Text(text = item, fontSize = 13.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan") },
                    placeholder = { Text("Misal: Setoran sore lunas") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirm(amount, paymentDate, method, notes)
                    }
                },
                modifier = Modifier.testTag("payment_save_confirm_button")
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
