package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PriceChange
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.StallEntity
import com.example.ui.theme.StatusGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun StallScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = viewModel.strings
    val stalls by viewModel.allStalls.collectAsStateWithLifecycle()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingStall by remember { mutableStateOf<StallEntity?>(null) }
    var stallToDelete by remember { mutableStateOf<StallEntity?>(null) }
    var showTrialLimitAlert by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (viewModel.isTrial && stalls.size >= com.example.util.AppConfig.TRIAL_MAX_STALLS) {
                        showTrialLimitAlert = true
                    } else {
                        editingStall = null
                        showAddEditDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("stall_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.tambahLapak)
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
            // Action banner: Go to Set Prices per Stall
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
                            .padding(14.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.hargaKhususLapak,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Atur harga jual berbeda untuk setiap lapak",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        OutlinedButton(
                            onClick = { viewModel.navigateTo(ScreenDestination.ATUR_HARGA) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("stall_btn_manage_prices")
                        ) {
                            Icon(Icons.Default.PriceChange, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Atur", fontSize = 12.sp)
                        }
                    }
                }
            }

            if (stalls.isEmpty()) {
                item {
                    Text(
                        text = strings.lapakKosong,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(stalls, key = { "stall_${it.id}" }) { stall ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stall_item_${stall.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Section 1: Top info row
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (stall.isActive) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = if (stall.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stall.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = if (stall.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                                    )
                                    if (stall.address.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "📍 ${stall.address}",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (stall.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "📝 ${stall.notes}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Status badge chip
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (stall.isActive) StatusGreen.copy(alpha = 0.12f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (stall.isActive) "Aktif" else "Nonaktif",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (stall.isActive) StatusGreen else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Section 2: Clean action bar with divider
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Left: Status Toggle Switch
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Status:",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = stall.isActive,
                                        onCheckedChange = { viewModel.toggleStallActive(stall) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                }

                                // Right: Edit & Delete buttons
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            editingStall = stall
                                            showAddEditDialog = true
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = strings.editLapak,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Edit", fontSize = 12.sp)
                                    }

                                    IconButton(
                                        onClick = { stallToDelete = stall },
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
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Stall Dialog
    if (showAddEditDialog) {
        AddEditStallDialog(
            stall = editingStall,
            strings = strings,
            onDismiss = { showAddEditDialog = false },
            onSave = { name, address, notes ->
                if (editingStall != null) {
                    viewModel.updateStall(
                        editingStall!!.copy(
                            name = name,
                            address = address,
                            notes = notes
                        )
                    )
                } else {
                    viewModel.addStall(name, address, notes)
                }
                showAddEditDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
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

    if (showTrialLimitAlert) {
        AlertDialog(
            onDismissRequest = { showTrialLimitAlert = false },
            title = {
                Text(
                    text = "Batas Versi Trial (2 Lapak)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column {
                    Text(
                        text = "Versi Trial dibatasi maksimal 2 lapak. Untuk menambah lapak tanpa batas dan menikmati akses permanen, silakan upgrade ke Full Version dengan menghubungi pengembang:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = com.example.util.AppConfig.DEVELOPER_EMAIL,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${com.example.util.AppConfig.DEVELOPER_EMAIL}")
                            putExtra(Intent.EXTRA_SUBJECT, "Upgrade Catat Jualan - Full Version")
                        }
                        try {
                            context.startActivity(Intent.createChooser(intent, "Kirim Email..."))
                        } catch (_: Exception) {}
                        showTrialLimitAlert = false
                    }
                ) {
                    Text("Hubungi via Email")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTrialLimitAlert = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
fun AddEditStallDialog(
    stall: StallEntity?,
    strings: com.example.model.AppStrings,
    onDismiss: () -> Unit,
    onSave: (name: String, address: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(stall?.name ?: "") }
    var address by remember { mutableStateOf(stall?.address ?: "") }
    var notes by remember { mutableStateOf(stall?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (stall == null) strings.tambahLapak else strings.editLapak) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.namaLapakLabel) },
                    placeholder = { Text("Contoh: Lapak A, Lapak Simpang") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_stall_name_input")
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(strings.alamatLapak) },
                    placeholder = { Text("Lokasi lapak...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_stall_address_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Tambahan (Opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), address.trim(), notes.trim())
                    }
                },
                modifier = Modifier.testTag("dialog_stall_save_button")
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
