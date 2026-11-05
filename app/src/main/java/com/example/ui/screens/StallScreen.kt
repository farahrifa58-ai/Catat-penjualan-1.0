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
    val strings = viewModel.strings
    val stalls by viewModel.allStalls.collectAsStateWithLifecycle()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingStall by remember { mutableStateOf<StallEntity?>(null) }
    var stallToDelete by remember { mutableStateOf<StallEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingStall = null
                    showAddEditDialog = true
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
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stall_item_${stall.id}")
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
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (stall.isActive) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = if (stall.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stall.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
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
                                    color = if (stall.isActive) StatusGreen else MaterialTheme.colorScheme.outline
                                )
                            }

                            // Active toggle
                            Switch(
                                checked = stall.isActive,
                                onCheckedChange = { viewModel.toggleStallActive(stall) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )

                            // Edit button
                            IconButton(onClick = {
                                editingStall = stall
                                showAddEditDialog = true
                            }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = strings.editLapak,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Delete button
                            IconButton(onClick = { stallToDelete = stall }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = strings.hapusLapak,
                                    tint = MaterialTheme.colorScheme.error
                                )
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
