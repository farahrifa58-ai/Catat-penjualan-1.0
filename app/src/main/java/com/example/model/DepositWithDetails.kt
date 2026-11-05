package com.example.model

import com.example.data.local.entity.DepositEntity
import com.example.data.local.entity.DepositItemEntity
import com.example.data.local.entity.PaymentEntity

enum class PaymentStatus {
    PAID_IN_FULL,      // Lunas
    PARTIALLY_PAID,    // Dibayar Sebagian
    UNPAID             // Belum Lunas
}

data class DepositWithDetails(
    val deposit: DepositEntity,
    val items: List<DepositItemEntity>,
    val payments: List<PaymentEntity>
) {
    val totalSent: Int
        get() = items.sumOf { it.quantitySent }

    val totalSold: Int
        get() = items.sumOf { it.quantitySold }

    val totalRemaining: Int
        get() = items.sumOf { it.remainingQty }

    val totalBill: Double
        get() = items.sumOf { it.subtotalBill }

    val totalPaid: Double
        get() = payments.sumOf { it.amountPaid }

    val remainingBill: Double
        get() = (totalBill - totalPaid).coerceAtLeast(0.0)

    val isSaleRecorded: Boolean
        get() = items.isNotEmpty() && items.any { it.hasRecordedSale }

    val isConditionComplete: Boolean
        get() = items.all { it.isConditionComplete }

    val paymentStatus: PaymentStatus
        get() = when {
            totalBill <= 0.0 -> PaymentStatus.UNPAID
            totalPaid >= totalBill -> PaymentStatus.PAID_IN_FULL
            totalPaid > 0.0 -> PaymentStatus.PARTIALLY_PAID
            else -> PaymentStatus.UNPAID
        }
}
