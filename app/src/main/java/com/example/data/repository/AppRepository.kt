package com.example.data.repository

import com.example.data.local.dao.AppDao
import com.example.data.local.dao.DepositAggregate
import com.example.data.local.entity.DepositEntity
import com.example.data.local.entity.DepositItemEntity
import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.StallEntity
import com.example.data.local.entity.StallMenuPriceEntity
import com.example.model.DepositWithDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AppRepository(private val appDao: AppDao) {

    // === Stalls (Lapak) ===
    val allStalls: Flow<List<StallEntity>> =
        appDao.getAllStalls().flowOn(Dispatchers.IO)

    val activeStalls: Flow<List<StallEntity>> =
        appDao.getActiveStalls().flowOn(Dispatchers.IO)

    suspend fun insertStall(stall: StallEntity): Long = withContext(Dispatchers.IO) {
        appDao.insertStall(stall)
    }

    suspend fun updateStall(stall: StallEntity) = withContext(Dispatchers.IO) {
        appDao.updateStall(stall)
    }

    suspend fun deleteStall(id: Long) = withContext(Dispatchers.IO) {
        appDao.deleteStallById(id)
    }

    // === Stall Menu Prices ===
    val allStallPrices: Flow<List<StallMenuPriceEntity>> =
        appDao.getAllStallPrices().flowOn(Dispatchers.IO)

    fun getPricesForStall(stallId: Long): Flow<List<StallMenuPriceEntity>> =
        appDao.getPricesForStall(stallId).flowOn(Dispatchers.IO)

    suspend fun getPriceForStallAndMenu(stallId: Long, menuId: Long): Double? = withContext(Dispatchers.IO) {
        appDao.getPriceForStallAndMenu(stallId, menuId)
    }

    suspend fun setStallPrice(stallId: Long, menuId: Long, price: Double) = withContext(Dispatchers.IO) {
        appDao.insertOrUpdateStallPrice(
            StallMenuPriceEntity(stallId = stallId, menuId = menuId, price = price)
        )
    }

    // === Menu Items ===
    val allMenuItems: Flow<List<MenuItemEntity>> =
        appDao.getAllMenuItems().flowOn(Dispatchers.IO)

    val activeMenuItems: Flow<List<MenuItemEntity>> =
        appDao.getActiveMenuItems().flowOn(Dispatchers.IO)

    suspend fun insertMenuItem(item: MenuItemEntity): Long = withContext(Dispatchers.IO) {
        appDao.insertMenuItem(item)
    }

    suspend fun updateMenuItem(item: MenuItemEntity) = withContext(Dispatchers.IO) {
        appDao.updateMenuItem(item)
    }

    suspend fun deleteMenuItem(id: Long) = withContext(Dispatchers.IO) {
        appDao.deleteMenuItemById(id)
    }

    suspend fun ensureInitialData() = withContext(Dispatchers.IO) {
        // Database dimulai 100% bersih tanpa produk dan tanpa lapak bawaan
    }

    // === Deposits & Aggregates ===
    val allDeposits: Flow<List<DepositWithDetails>> = appDao.getAllDepositAggregates()
        .map { aggregates -> aggregates.map { it.toModel() } }
        .flowOn(Dispatchers.IO)

    fun getDepositsByDate(date: String): Flow<List<DepositWithDetails>> =
        appDao.getDepositAggregatesByDate(date)
            .map { aggregates -> aggregates.map { it.toModel() } }
            .flowOn(Dispatchers.IO)

    suspend fun getDepositById(id: Long): DepositWithDetails? = withContext(Dispatchers.IO) {
        appDao.getDepositAggregateById(id)?.toModel()
    }

    suspend fun createDeposit(
        deposit: DepositEntity,
        items: List<DepositItemEntity>
    ): Long = withContext(Dispatchers.IO) {
        val depositId = appDao.insertDeposit(deposit)
        val linkedItems = items.map { it.copy(depositId = depositId) }
        appDao.insertDepositItems(linkedItems)
        depositId
    }

    suspend fun updateDeposit(
        deposit: DepositEntity,
        items: List<DepositItemEntity>
    ) = withContext(Dispatchers.IO) {
        appDao.updateDeposit(deposit)
        appDao.deleteDepositItemsByDepositId(deposit.id)
        val linkedItems = items.map { it.copy(depositId = deposit.id) }
        appDao.insertDepositItems(linkedItems)
    }

    suspend fun updateSalesResult(
        items: List<DepositItemEntity>
    ) = withContext(Dispatchers.IO) {
        appDao.updateDepositItems(items)
    }

    suspend fun addPayment(payment: PaymentEntity): Long = withContext(Dispatchers.IO) {
        appDao.insertPayment(payment)
    }

    suspend fun deleteDeposit(depositId: Long) = withContext(Dispatchers.IO) {
        appDao.deleteDepositById(depositId)
    }

    suspend fun deletePayment(paymentId: Long) = withContext(Dispatchers.IO) {
        appDao.deletePaymentById(paymentId)
    }

    suspend fun loadDemoData(todayDate: String = "", yesterdayDate: String = "") = withContext(Dispatchers.IO) {
        appDao.clearPayments()
        appDao.clearDepositItems()
        appDao.clearDeposits()
        appDao.clearStallPrices()
        appDao.clearStalls()
        appDao.clearMenuItems()

        // Sesuai permintaan pengguna: semua contoh produk dan lapak dihapus.
        // Pengguna baru bebas menambahkan produk dan lapak mereka sendiri.
    }

    private fun DepositAggregate.toModel(): DepositWithDetails {
        return DepositWithDetails(
            deposit = deposit,
            items = items,
            payments = payments
        )
    }
}
