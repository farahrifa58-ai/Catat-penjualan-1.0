package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deposits")
data class DepositEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format YYYY-MM-DD
    val stallId: Long? = null,
    val stallName: String = "", // Snapshot of stall name at transaction time
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
