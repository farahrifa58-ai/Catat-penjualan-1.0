package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stalls")
data class StallEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val address: String = "",
    val notes: String = "",
    val photoUri: String? = null, // Path atau URI foto lapak
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
