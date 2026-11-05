package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val hpp: Double = 0.0, // Harga Pokok Produksi / Biaya Modal
    val price: Double = 0.0, // Harga jual standar
    val unit: String = "pcs",
    val photoUri: String? = null, // Path atau URI foto menu makanan
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
