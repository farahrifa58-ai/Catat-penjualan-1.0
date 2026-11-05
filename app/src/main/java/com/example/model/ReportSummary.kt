package com.example.model

data class MenuItemPerformance(
    val menuName: String,
    val totalSold: Int,
    val totalRemaining: Int,
    val totalDamaged: Int,
    val revenue: Double
)

data class StallPerformance(
    val stallName: String,
    val totalSent: Int,
    val totalSold: Int,
    val totalRemaining: Int,
    val revenue: Double,
    val cashReceived: Double,
    val receivable: Double,
    val topMenuName: String
)

data class DailyTrend(
    val date: String,
    val displayLabel: String,
    val revenue: Double,
    val soldCount: Int
)

data class ReportSummary(
    val totalSent: Int,
    val totalSold: Int,
    val totalRemaining: Int,
    val totalRevenue: Double,
    val totalCashReceived: Double,
    val totalReceivable: Double,
    val totalDamaged: Int,
    val bestSellingItems: List<MenuItemPerformance>,
    val mostRemainingItems: List<MenuItemPerformance>,
    val stallPerformances: List<StallPerformance>,
    val dailyTrends: List<DailyTrend>
) {
    companion object {
        fun calculate(
            deposits: List<DepositWithDetails>,
            filterStallId: Long? = null,
            filterMenuName: String? = null
        ): ReportSummary {
            var sent = 0
            var sold = 0
            var remaining = 0
            var revenue = 0.0
            var cash = 0.0
            var damaged = 0

            val menuMap = mutableMapOf<String, MutablePerformance>()
            val dateTrendMap = mutableMapOf<String, MutableTrend>()
            val stallMap = mutableMapOf<String, MutableStallPerformance>()

            // Filter deposits if filterStallId is specified
            val targetDeposits = deposits.filter { d ->
                filterStallId == null || d.deposit.stallId == filterStallId
            }

            targetDeposits.forEach { d ->
                val stallName = if (d.deposit.stallName.isNotBlank()) d.deposit.stallName else "Lapak"
                val stallPerf = stallMap.getOrPut(stallName) {
                    MutableStallPerformance(stallName, 0, 0, 0, 0.0, 0.0, mutableMapOf())
                }
                stallPerf.cash += d.totalPaid

                cash += d.totalPaid

                val dateKey = d.deposit.date
                val trend = dateTrendMap.getOrPut(dateKey) { MutableTrend(dateKey, 0.0, 0) }

                d.items.forEach { item ->
                    if (filterMenuName == null || item.menuName.equals(filterMenuName, ignoreCase = true)) {
                        sent += item.quantitySent
                        sold += item.quantitySold
                        remaining += item.remainingQty
                        revenue += item.subtotalBill
                        damaged += item.damagedQty

                        trend.revenue += item.subtotalBill
                        trend.soldCount += item.quantitySold

                        stallPerf.sent += item.quantitySent
                        stallPerf.sold += item.quantitySold
                        stallPerf.remaining += item.remainingQty
                        stallPerf.revenue += item.subtotalBill
                        stallPerf.menuSales[item.menuName] =
                            (stallPerf.menuSales[item.menuName] ?: 0) + item.quantitySold

                        val perf = menuMap.getOrPut(item.menuName) {
                            MutablePerformance(item.menuName, 0, 0, 0, 0.0)
                        }
                        perf.sold += item.quantitySold
                        perf.remaining += item.remainingQty
                        perf.damaged += item.damagedQty
                        perf.revenue += item.subtotalBill
                    }
                }
            }

            val receivables = (revenue - cash).coerceAtLeast(0.0)

            val performances = menuMap.values.map {
                MenuItemPerformance(
                    menuName = it.name,
                    totalSold = it.sold,
                    totalRemaining = it.remaining,
                    totalDamaged = it.damaged,
                    revenue = it.revenue
                )
            }

            val bestSelling = performances.sortedByDescending { it.totalSold }
            val mostRemaining = performances.sortedByDescending { it.totalRemaining }

            val stallList = stallMap.values.map { s ->
                val topMenu = s.menuSales.maxByOrNull { it.value }?.key ?: "-"
                StallPerformance(
                    stallName = s.name,
                    totalSent = s.sent,
                    totalSold = s.sold,
                    totalRemaining = s.remaining,
                    revenue = s.revenue,
                    cashReceived = s.cash,
                    receivable = (s.revenue - s.cash).coerceAtLeast(0.0),
                    topMenuName = topMenu
                )
            }.sortedByDescending { it.revenue }

            val dailyTrends = dateTrendMap.values
                .sortedBy { it.date }
                .takeLast(7)
                .map {
                    val label = if (it.date.length >= 10) it.date.substring(5) else it.date
                    DailyTrend(
                        date = it.date,
                        displayLabel = label,
                        revenue = it.revenue,
                        soldCount = it.soldCount
                    )
                }

            return ReportSummary(
                totalSent = sent,
                totalSold = sold,
                totalRemaining = remaining,
                totalRevenue = revenue,
                totalCashReceived = cash,
                totalReceivable = receivables,
                totalDamaged = damaged,
                bestSellingItems = bestSelling,
                mostRemainingItems = mostRemaining,
                stallPerformances = stallList,
                dailyTrends = dailyTrends
            )
        }
    }

    private class MutablePerformance(
        val name: String,
        var sold: Int,
        var remaining: Int,
        var damaged: Int,
        var revenue: Double
    )

    private class MutableStallPerformance(
        val name: String,
        var sent: Int,
        var sold: Int,
        var remaining: Int,
        var revenue: Double,
        var cash: Double,
        val menuSales: MutableMap<String, Int>
    )

    private class MutableTrend(
        val date: String,
        var revenue: Double,
        var soldCount: Int
    )
}
