package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "deposit_items",
    foreignKeys = [
        ForeignKey(
            entity = DepositEntity::class,
            parentColumns = ["id"],
            childColumns = ["depositId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["depositId"])]
)
data class DepositItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val depositId: Long,
    val menuId: Long? = null,
    val menuName: String,
    val priceSnapshot: Double,
    val unit: String = "pcs",
    val quantitySent: Int,
    val quantitySold: Int = 0,
    val hasRecordedSale: Boolean = false,
    val resaleableQty: Int = 0,
    val damagedQty: Int = 0,
    val selfConsumedQty: Int = 0
) {
    val remainingQty: Int
        get() = (quantitySent - quantitySold).coerceAtLeast(0)

    val subtotalBill: Double
        get() = quantitySold * priceSnapshot

    val conditionSum: Int
        get() = resaleableQty + damagedQty + selfConsumedQty

    val isConditionComplete: Boolean
        get() = !hasRecordedSale || conditionSum == remainingQty
}
