package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stall_menu_prices",
    foreignKeys = [
        ForeignKey(
            entity = StallEntity::class,
            parentColumns = ["id"],
            childColumns = ["stallId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MenuItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["menuId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["stallId", "menuId"], unique = true),
        Index(value = ["stallId"]),
        Index(value = ["menuId"])
    ]
)
data class StallMenuPriceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val stallId: Long,
    val menuId: Long,
    val price: Double
)
