package com.example

import com.example.data.local.entity.DepositEntity
import com.example.data.local.entity.DepositItemEntity
import com.example.data.local.entity.PaymentEntity
import com.example.model.DepositWithDetails
import com.example.model.PaymentStatus
import com.example.model.ReportSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun verify_sales_bill_and_sisa_calculation_from_brief() {
        // Scenario from user brief:
        // Burger: Dikirim 20, Terjual 17, Sisa 3 (17 x 10.000 = 170.000)
        // Sandwich: Dikirim 15, Terjual 12, Sisa 3 (12 x 12.000 = 144.000)
        // Spaghetti: Dikirim 20, Terjual 18, Sisa 2 (18 x 15.000 = 270.000)
        // Total setoran = 55 makanan. Total terjual = 47. Total tagihan = 584.000

        val items = listOf(
            DepositItemEntity(
                depositId = 1,
                menuName = "Burger",
                priceSnapshot = 10000.0,
                unit = "pcs",
                quantitySent = 20,
                quantitySold = 17,
                hasRecordedSale = true,
                resaleableQty = 2,
                damagedQty = 1,
                selfConsumedQty = 0
            ),
            DepositItemEntity(
                depositId = 1,
                menuName = "Sandwich",
                priceSnapshot = 12000.0,
                unit = "pcs",
                quantitySent = 15,
                quantitySold = 12,
                hasRecordedSale = true,
                resaleableQty = 2,
                damagedQty = 0,
                selfConsumedQty = 1
            ),
            DepositItemEntity(
                depositId = 1,
                menuName = "Spaghetti",
                priceSnapshot = 15000.0,
                unit = "porsi",
                quantitySent = 20,
                quantitySold = 18,
                hasRecordedSale = true,
                resaleableQty = 2,
                damagedQty = 0,
                selfConsumedQty = 0
            )
        )

        val deposit = DepositWithDetails(
            deposit = DepositEntity(id = 1, date = "2026-11-04", stallId = 1L, stallName = "Lapak A"),
            items = items,
            payments = listOf(
                PaymentEntity(depositId = 1, amountPaid = 400000.0, paymentDate = "2026-11-04")
            )
        )

        assertEquals(55, deposit.totalSent)
        assertEquals(47, deposit.totalSold)
        assertEquals(8, deposit.totalRemaining)
        assertEquals(584000.0, deposit.totalBill, 0.01)
        assertEquals(400000.0, deposit.totalPaid, 0.01)
        assertEquals(184000.0, deposit.remainingBill, 0.01)
        assertEquals(PaymentStatus.PARTIALLY_PAID, deposit.paymentStatus)
        assertTrue(deposit.isConditionComplete)
    }

    @Test
    fun verify_multi_stall_pricing_and_reports() {
        // Lapak A: Burger @ 10.000 (10 sold = 100.000)
        val depA = DepositWithDetails(
            deposit = DepositEntity(id = 1, date = "2026-11-05", stallId = 101L, stallName = "Lapak A"),
            items = listOf(
                DepositItemEntity(
                    depositId = 1,
                    menuName = "Burger",
                    priceSnapshot = 10000.0,
                    unit = "pcs",
                    quantitySent = 10,
                    quantitySold = 10,
                    hasRecordedSale = true
                )
            ),
            payments = listOf(PaymentEntity(depositId = 1, amountPaid = 100000.0, paymentDate = "2026-11-05"))
        )

        // Lapak B: Burger @ 11.000 (10 sold = 110.000)
        val depB = DepositWithDetails(
            deposit = DepositEntity(id = 2, date = "2026-11-05", stallId = 102L, stallName = "Lapak B"),
            items = listOf(
                DepositItemEntity(
                    depositId = 2,
                    menuName = "Burger",
                    priceSnapshot = 11000.0,
                    unit = "pcs",
                    quantitySent = 10,
                    quantitySold = 10,
                    hasRecordedSale = true
                )
            ),
            payments = listOf(PaymentEntity(depositId = 2, amountPaid = 60000.0, paymentDate = "2026-11-05"))
        )

        val allDeposits = listOf(depA, depB)

        // Global report: Total Revenue = 100.000 + 110.000 = 210.000, Total Cash = 160.000, Piutang = 50.000
        val globalSummary = ReportSummary.calculate(allDeposits)
        assertEquals(210000.0, globalSummary.totalRevenue, 0.01)
        assertEquals(160000.0, globalSummary.totalCashReceived, 0.01)
        assertEquals(50000.0, globalSummary.totalReceivable, 0.01)
        assertEquals(2, globalSummary.stallPerformances.size)

        // Filter by Lapak A only
        val lapakASummary = ReportSummary.calculate(allDeposits, filterStallId = 101L)
        assertEquals(100000.0, lapakASummary.totalRevenue, 0.01)
        assertEquals(100000.0, lapakASummary.totalCashReceived, 0.01)
        assertEquals(0.0, lapakASummary.totalReceivable, 0.01)

        // Filter by Lapak B only
        val lapakBSummary = ReportSummary.calculate(allDeposits, filterStallId = 102L)
        assertEquals(110000.0, lapakBSummary.totalRevenue, 0.01)
        assertEquals(60000.0, lapakBSummary.totalCashReceived, 0.01)
        assertEquals(50000.0, lapakBSummary.totalReceivable, 0.01)
    }

    @Test
    fun verify_dot_number_formatting() {
        assertEquals("1.000", com.example.util.FormatUtils.formatNumber(1000.0))
        assertEquals("10.000", com.example.util.FormatUtils.formatNumber(10000.0))
        assertEquals("100.000", com.example.util.FormatUtils.formatNumber(100000.0))
        assertEquals("Rp 10.000", com.example.util.FormatUtils.formatRupiah(10000.0))
        assertEquals(10000.0, com.example.util.FormatUtils.parseAmount("10.000"), 0.01)
    }
}
