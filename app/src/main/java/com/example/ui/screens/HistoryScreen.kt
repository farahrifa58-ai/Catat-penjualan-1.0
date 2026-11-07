package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppLanguage
import com.example.model.DepositWithDetails
import com.example.model.ReportSummary
import com.example.ui.components.PaymentStatusChip
import com.example.ui.components.StatCard
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.MainViewModel
import com.example.util.FormatUtils

enum class HistoryFilter {
    ALL,
    TODAY,
    WEEK,
    MONTH
}

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = viewModel.strings
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isEnglish = language == AppLanguage.ENGLISH
    val allDeposits by viewModel.allDeposits.collectAsStateWithLifecycle()
    val allStalls by viewModel.allStalls.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Riwayat, 1: Laporan
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf(HistoryFilter.ALL) }
    var selectedStallId by remember { mutableStateOf<Long?>(null) } // null = Semua Lapak

    val todayIso = FormatUtils.getTodayDateIso()

    // Filter deposits based on selected filterType, stall, and search query
    val filteredDeposits = remember(allDeposits, filterType, searchQuery, selectedStallId) {
        allDeposits.filter { dep ->
            val matchSearch = searchQuery.isBlank() ||
                    dep.deposit.stallName.contains(searchQuery, ignoreCase = true) ||
                    dep.deposit.date.contains(searchQuery)

            val matchFilter = when (filterType) {
                HistoryFilter.ALL -> true
                HistoryFilter.TODAY -> dep.deposit.date == todayIso
                HistoryFilter.WEEK -> dep.deposit.date >= FormatUtils.shiftDays(todayIso, -7)
                HistoryFilter.MONTH -> dep.deposit.date.take(7) == todayIso.take(7)
            }

            val matchStall = selectedStallId == null || dep.deposit.stallId == selectedStallId

            matchSearch && matchFilter && matchStall
        }
    }

    val reportSummary = remember(filteredDeposits, selectedStallId) {
        ReportSummary.calculate(filteredDeposits, filterStallId = selectedStallId)
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Tab Row: [Riwayat Transaksi] | [Laporan Usaha]
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = strings.tabRiwayat, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("tab_history")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = strings.tabLaporan, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("tab_report")
            )
        }

        // Filter Bar Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(top = 8.dp, bottom = 4.dp)
        ) {
            // Row 1: Stall Filter Horizontal Chips (Smooth Horizontal Scroll)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp)
            ) {
                FilterChip(
                    selected = selectedStallId == null,
                    onClick = { selectedStallId = null },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Store,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text(strings.semuaLapak, fontSize = 12.sp, fontWeight = if (selectedStallId == null) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                allStalls.forEach { stall ->
                    FilterChip(
                        selected = selectedStallId == stall.id,
                        onClick = { selectedStallId = stall.id },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Storefront,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(stall.name, fontSize = 12.sp, fontWeight = if (selectedStallId == stall.id) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Row 2: Date Filter Horizontal Chips (Smooth Horizontal Scroll to prevent squishing)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                FilterChip(
                    selected = filterType == HistoryFilter.ALL,
                    onClick = { filterType = HistoryFilter.ALL },
                    leadingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    label = { Text(strings.filterSemua, fontSize = 12.sp, fontWeight = if (filterType == HistoryFilter.ALL) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
                FilterChip(
                    selected = filterType == HistoryFilter.TODAY,
                    onClick = { filterType = HistoryFilter.TODAY },
                    leadingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    label = { Text(strings.filterHariIni, fontSize = 12.sp, fontWeight = if (filterType == HistoryFilter.TODAY) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
                FilterChip(
                    selected = filterType == HistoryFilter.WEEK,
                    onClick = { filterType = HistoryFilter.WEEK },
                    leadingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    label = { Text(strings.filterMingguIni, fontSize = 12.sp, fontWeight = if (filterType == HistoryFilter.WEEK) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
                FilterChip(
                    selected = filterType == HistoryFilter.MONTH,
                    onClick = { filterType = HistoryFilter.MONTH },
                    leadingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    label = { Text(strings.filterBulanIni, fontSize = 12.sp, fontWeight = if (filterType == HistoryFilter.MONTH) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }

        if (selectedTab == 0) {
            // === TAB 1: RIWAYAT TRANSAKSI ===
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search Field
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(strings.cariLapak, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_search_field")
                    )
                }

                if (filteredDeposits.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Belum Ada Riwayat Transaksi",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = strings.tidakAdaAktivitas,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(filteredDeposits, key = { "hist_dep_${it.deposit.id}" }) { dep ->
                        HistoryItemCard(
                            deposit = dep,
                            strings = strings,
                            isEnglish = isEnglish,
                            onClick = { viewModel.selectDepositForDetail(dep) }
                        )
                    }
                }
            }
        } else {
            // === TAB 2: LAPORAN USAHA & MULTI-LAPAK ===
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Export CSV action
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = if (selectedStallId != null) {
                                    val sName = allStalls.find { it.id == selectedStallId }?.name ?: ""
                                    "Laporan: $sName"
                                } else strings.tabLaporan,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${filteredDeposits.size} transaksi terfilter",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val csv = generateCsvReport(filteredDeposits)
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, strings.bagikanLaporan)
                                context.startActivity(shareIntent)
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("report_export_csv_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.eksporCsv, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Summary Numbers Grid
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard(
                            title = strings.totalDikirim,
                            value = "${reportSummary.totalSent} item",
                            icon = Icons.Default.History,
                            iconTint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = strings.totalTerjual,
                            value = "${reportSummary.totalSold} item",
                            icon = Icons.Default.Assessment,
                            iconTint = StatusGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard(
                            title = strings.totalOmzet,
                            value = FormatUtils.formatRupiah(reportSummary.totalRevenue),
                            icon = Icons.Default.Assessment,
                            iconTint = StatusGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = strings.totalUangDiterima,
                            value = FormatUtils.formatRupiah(reportSummary.totalCashReceived),
                            icon = Icons.Default.History,
                            iconTint = Color(0xFF1565C0),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard(
                            title = strings.sisaPiutang,
                            value = FormatUtils.formatRupiah(reportSummary.totalReceivable),
                            icon = Icons.Default.Assessment,
                            iconTint = if (reportSummary.totalReceivable > 0) StatusRed else StatusGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = strings.makananRusak,
                            value = "${reportSummary.totalDamaged} item",
                            icon = Icons.Default.History,
                            iconTint = StatusAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Section F Requirement: Performance Breakdown Per Lapak
                if (reportSummary.stallPerformances.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.laporanPerLapak,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    items(reportSummary.stallPerformances, key = { "stall_perf_${it.stallName}" }) { stallPerf ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                        ) {
                                            Icon(
                                                Icons.Default.Storefront,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = stallPerf.stallName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Text(
                                        text = FormatUtils.formatRupiah(stallPerf.revenue),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = StatusGreen
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Kirim: ${stallPerf.totalSent} | Jual: ${stallPerf.totalSold} | Sisa: ${stallPerf.totalRemaining}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (stallPerf.receivable > 0) {
                                        Text(
                                            text = "Piutang: ${FormatUtils.formatRupiah(stallPerf.receivable)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = StatusRed
                                        )
                                    } else {
                                        Text(
                                            text = strings.statusLunas,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = StatusGreen
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${strings.menuTerlarisPerLapak}: ${stallPerf.topMenuName}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Sales Trend Chart (Compose Canvas)
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = strings.grafikPenjualan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            if (reportSummary.dailyTrends.isEmpty()) {
                                Text(
                                    text = strings.belumAdaPenjualan,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                val trends = reportSummary.dailyTrends
                                val maxRev = trends.maxOfOrNull { it.revenue }?.coerceAtLeast(1.0) ?: 1.0

                                Column {
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .padding(horizontal = 8.dp)
                                    ) {
                                        val barCount = trends.size
                                        val totalWidth = size.width
                                        val totalHeight = size.height
                                        val slotWidth = totalWidth / barCount
                                        val barWidth = (slotWidth * 0.45f).coerceAtMost(40f)

                                        trends.forEachIndexed { i, t ->
                                            val fraction = (t.revenue / maxRev).toFloat()
                                            val barHeight = (fraction * (totalHeight - 20f)).coerceAtLeast(6f)
                                            val left = i * slotWidth + (slotWidth - barWidth) / 2
                                            val top = totalHeight - barHeight

                                            drawRoundRect(
                                                color = Color(0xFF2E7D32),
                                                topLeft = Offset(left, top),
                                                size = Size(barWidth, barHeight),
                                                cornerRadius = CornerRadius(6f, 6f)
                                            )
                                        }
                                    }

                                    // X-axis date labels
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp)
                                    ) {
                                        trends.forEach {
                                            Text(
                                                text = it.displayLabel,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Best Selling Foods
                item {
                    Text(
                        text = strings.menuTerlaris,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(reportSummary.bestSellingItems.take(5)) { perf ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                                Text(text = perf.menuName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = "${strings.totalTerjual}: ${perf.totalSold} item • ${strings.totalOmzet}: ${FormatUtils.formatRupiah(perf.revenue)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${perf.totalSold}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = StatusGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    deposit: DepositWithDetails,
    strings: com.example.model.AppStrings,
    isEnglish: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("history_item_card_${deposit.deposit.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Lapak Name + Payment Status Badge
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
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (deposit.deposit.stallName.isNotBlank()) deposit.deposit.stallName else "Setoran Lapak",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = FormatUtils.formatDateDisplay(deposit.deposit.date, isEnglish),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (deposit.isHoliday) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StatusAmber.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🏖️ Libur",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusAmber
                        )
                    }
                } else {
                    PaymentStatusChip(status = deposit.paymentStatus, strings = strings)
                }
            }

            if (deposit.isHoliday) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(StatusAmber.copy(alpha = 0.1f))
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BeachAccess,
                        contentDescription = null,
                        tint = StatusAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (deposit.deposit.notes.isNotBlank() && deposit.deposit.notes != "Lapak Libur") {
                            "Lapak Libur: \"${deposit.deposit.notes}\""
                        } else {
                            "Lapak Libur (Tidak ada setoran makanan)"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))

                // 3-Column Pill Box: Kirim | Terjual | Sisa
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Kirim", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${deposit.totalSent}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(StatusGreen.copy(alpha = 0.12f))
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Terjual", fontSize = 10.sp, color = StatusGreen, fontWeight = FontWeight.SemiBold)
                            Text(text = "${deposit.totalSold}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StatusGreen)
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(StatusAmber.copy(alpha = 0.14f))
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Sisa", fontSize = 10.sp, color = StatusAmber, fontWeight = FontWeight.SemiBold)
                            Text(text = "${deposit.totalRemaining}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StatusAmber)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Financial Summary Bar & Arrow
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            text = "Tagihan: ${FormatUtils.formatRupiah(deposit.totalBill)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Dibayar: ${FormatUtils.formatRupiah(deposit.totalPaid)}" +
                                    if (deposit.remainingBill > 0) " • Piutang: ${FormatUtils.formatRupiah(deposit.remainingBill)}" else " • Lunas",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (deposit.remainingBill > 0) StatusRed else StatusGreen
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Detail", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Detail",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

fun generateCsvReport(deposits: List<DepositWithDetails>): String {
    val sb = StringBuilder()
    sb.append("Tanggal,Lapak,Total Dikirim,Total Terjual,Total Sisa,Total Tagihan,Total Dibayar,Status\n")
    deposits.forEach { d ->
        sb.append("${d.deposit.date},")
        sb.append("\"${d.deposit.stallName.replace("\"", "\"\"")}\",")
        sb.append("${d.totalSent},")
        sb.append("${d.totalSold},")
        sb.append("${d.totalRemaining},")
        sb.append("${d.totalBill.toLong()},")
        sb.append("${d.totalPaid.toLong()},")
        sb.append("${d.paymentStatus.name}\n")
    }
    return sb.toString()
}
