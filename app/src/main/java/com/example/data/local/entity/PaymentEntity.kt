package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
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
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val depositId: Long,
    val amountPaid: Double,
    val paymentDate: String, // Format YYYY-MM-DD
    val paymentMethod: String = "Tunai",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
