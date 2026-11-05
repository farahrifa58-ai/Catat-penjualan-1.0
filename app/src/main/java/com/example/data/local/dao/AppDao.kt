package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.DepositEntity
import com.example.data.local.entity.DepositItemEntity
import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.StallEntity
import com.example.data.local.entity.StallMenuPriceEntity
import kotlinx.coroutines.flow.Flow

data class DepositAggregate(
    @Embedded val deposit: DepositEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "depositId"
    )
    val items: List<DepositItemEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "depositId"
    )
    val payments: List<PaymentEntity>
)

@Dao
interface AppDao {

    // === Stalls (Lapak) ===
    @Query("SELECT * FROM stalls ORDER BY isActive DESC, name ASC")
    fun getAllStalls(): Flow<List<StallEntity>>

    @Query("SELECT * FROM stalls WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveStalls(): Flow<List<StallEntity>>

    @Query("SELECT * FROM stalls WHERE id = :id LIMIT 1")
    suspend fun getStallById(id: Long): StallEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStall(stall: StallEntity): Long

    @Update
    suspend fun updateStall(stall: StallEntity)

    @Query("DELETE FROM stalls WHERE id = :id")
    suspend fun deleteStallById(id: Long)

    @Query("SELECT COUNT(*) FROM stalls")
    suspend fun getStallCount(): Int

    // === Stall Menu Prices ===
    @Query("SELECT * FROM stall_menu_prices")
    fun getAllStallPrices(): Flow<List<StallMenuPriceEntity>>

    @Query("SELECT * FROM stall_menu_prices WHERE stallId = :stallId")
    fun getPricesForStall(stallId: Long): Flow<List<StallMenuPriceEntity>>

    @Query("SELECT price FROM stall_menu_prices WHERE stallId = :stallId AND menuId = :menuId LIMIT 1")
    suspend fun getPriceForStallAndMenu(stallId: Long, menuId: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStallPrice(entity: StallMenuPriceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStallPrices(entities: List<StallMenuPriceEntity>)

    @Query("DELETE FROM stall_menu_prices WHERE stallId = :stallId AND menuId = :menuId")
    suspend fun deleteStallPrice(stallId: Long, menuId: Long)

    // === Menu Items ===
    @Query("SELECT * FROM menu_items ORDER BY isActive DESC, name ASC")
    fun getAllMenuItems(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveMenuItems(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE id = :id LIMIT 1")
    suspend fun getMenuItemById(id: Long): MenuItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(menuItem: MenuItemEntity): Long

    @Update
    suspend fun updateMenuItem(menuItem: MenuItemEntity)

    @Delete
    suspend fun deleteMenuItem(menuItem: MenuItemEntity)

    @Query("DELETE FROM menu_items WHERE id = :id")
    suspend fun deleteMenuItemById(id: Long)

    @Query("SELECT COUNT(*) FROM menu_items")
    suspend fun getMenuCount(): Int

    // === Deposits ===
    @Transaction
    @Query("SELECT * FROM deposits ORDER BY date DESC, id DESC")
    fun getAllDepositAggregates(): Flow<List<DepositAggregate>>

    @Transaction
    @Query("SELECT * FROM deposits WHERE date = :date ORDER BY id DESC")
    fun getDepositAggregatesByDate(date: String): Flow<List<DepositAggregate>>

    @Transaction
    @Query("SELECT * FROM deposits WHERE stallId = :stallId ORDER BY date DESC, id DESC")
    fun getDepositAggregatesByStall(stallId: Long): Flow<List<DepositAggregate>>

    @Transaction
    @Query("SELECT * FROM deposits WHERE id = :id LIMIT 1")
    suspend fun getDepositAggregateById(id: Long): DepositAggregate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposit(deposit: DepositEntity): Long

    @Update
    suspend fun updateDeposit(deposit: DepositEntity)

    @Query("DELETE FROM deposits WHERE id = :id")
    suspend fun deleteDepositById(id: Long)

    // === Deposit Items ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepositItems(items: List<DepositItemEntity>)

    @Update
    suspend fun updateDepositItem(item: DepositItemEntity)

    @Update
    suspend fun updateDepositItems(items: List<DepositItemEntity>)

    @Query("DELETE FROM deposit_items WHERE depositId = :depositId")
    suspend fun deleteDepositItemsByDepositId(depositId: Long)

    // === Payments ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Query("SELECT * FROM payments WHERE depositId = :depositId ORDER BY createdAt ASC")
    fun getPaymentsByDepositId(depositId: Long): Flow<List<PaymentEntity>>

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePaymentById(id: Long)

    // === Clear for demo reload ===
    @Query("DELETE FROM payments")
    suspend fun clearPayments()

    @Query("DELETE FROM deposit_items")
    suspend fun clearDepositItems()

    @Query("DELETE FROM deposits")
    suspend fun clearDeposits()

    @Query("DELETE FROM stall_menu_prices")
    suspend fun clearStallPrices()

    @Query("DELETE FROM stalls")
    suspend fun clearStalls()

    @Query("DELETE FROM menu_items")
    suspend fun clearMenuItems()

    // === Raw queries for Google Drive Backup & Restore ===
    @Query("SELECT * FROM stalls")
    suspend fun getAllStallsList(): List<StallEntity>

    @Query("SELECT * FROM menu_items")
    suspend fun getAllMenuItemsList(): List<MenuItemEntity>

    @Query("SELECT * FROM stall_menu_prices")
    suspend fun getAllStallPricesList(): List<StallMenuPriceEntity>

    @Query("SELECT * FROM deposits")
    suspend fun getAllDepositsList(): List<DepositEntity>

    @Query("SELECT * FROM deposit_items")
    suspend fun getAllDepositItemsList(): List<DepositItemEntity>

    @Query("SELECT * FROM payments")
    suspend fun getAllPaymentsList(): List<PaymentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStalls(stalls: List<StallEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(items: List<MenuItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposits(deposits: List<DepositEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)
}
