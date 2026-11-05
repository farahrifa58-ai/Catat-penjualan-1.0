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
import org.json.JSONArray
import org.json.JSONObject

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

    // === Google Drive Backup & Restore Operations ===
    suspend fun createBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "CatatJualan")
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        // 1. Stalls
        val stallsArray = JSONArray()
        appDao.getAllStallsList().forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("name", s.name)
            obj.put("address", s.address)
            obj.put("notes", s.notes)
            obj.put("isActive", s.isActive)
            obj.put("photoUri", s.photoUri ?: "")
            stallsArray.put(obj)
        }
        root.put("stalls", stallsArray)

        // 2. Menu Items
        val menusArray = JSONArray()
        appDao.getAllMenuItemsList().forEach { m ->
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("name", m.name)
            obj.put("hpp", m.hpp)
            obj.put("price", m.price)
            obj.put("unit", m.unit)
            obj.put("isActive", m.isActive)
            obj.put("photoUri", m.photoUri ?: "")
            menusArray.put(obj)
        }
        root.put("menu_items", menusArray)

        // 3. Stall Menu Prices
        val pricesArray = JSONArray()
        appDao.getAllStallPricesList().forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("stallId", p.stallId)
            obj.put("menuId", p.menuId)
            obj.put("price", p.price)
            pricesArray.put(obj)
        }
        root.put("stall_menu_prices", pricesArray)

        // 4. Deposits
        val depositsArray = JSONArray()
        appDao.getAllDepositsList().forEach { d ->
            val obj = JSONObject()
            obj.put("id", d.id)
            obj.put("stallId", d.stallId ?: -1L)
            obj.put("stallName", d.stallName)
            obj.put("date", d.date)
            obj.put("notes", d.notes)
            obj.put("createdAt", d.createdAt)
            depositsArray.put(obj)
        }
        root.put("deposits", depositsArray)

        // 5. Deposit Items
        val depositItemsArray = JSONArray()
        appDao.getAllDepositItemsList().forEach { di ->
            val obj = JSONObject()
            obj.put("id", di.id)
            obj.put("depositId", di.depositId)
            obj.put("menuId", di.menuId ?: -1L)
            obj.put("menuName", di.menuName)
            obj.put("unit", di.unit)
            obj.put("priceSnapshot", di.priceSnapshot)
            obj.put("quantitySent", di.quantitySent)
            obj.put("quantitySold", di.quantitySold)
            obj.put("damagedQty", di.damagedQty)
            obj.put("resaleableQty", di.resaleableQty)
            obj.put("selfConsumedQty", di.selfConsumedQty)
            depositItemsArray.put(obj)
        }
        root.put("deposit_items", depositItemsArray)

        // 6. Payments
        val paymentsArray = JSONArray()
        appDao.getAllPaymentsList().forEach { py ->
            val obj = JSONObject()
            obj.put("id", py.id)
            obj.put("depositId", py.depositId)
            obj.put("amountPaid", py.amountPaid)
            obj.put("paymentDate", py.paymentDate)
            obj.put("paymentMethod", py.paymentMethod)
            obj.put("notes", py.notes)
            obj.put("createdAt", py.createdAt)
            paymentsArray.put(obj)
        }
        root.put("payments", paymentsArray)

        root.toString(2)
    }

    suspend fun restoreFromBackupJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("app") || root.getString("app") != "CatatJualan") {
                return@withContext false
            }

            // Clear current data safely
            appDao.clearPayments()
            appDao.clearDepositItems()
            appDao.clearDeposits()
            appDao.clearStallPrices()
            appDao.clearStalls()
            appDao.clearMenuItems()

            // 1. Restore Stalls
            if (root.has("stalls")) {
                val arr = root.getJSONArray("stalls")
                val list = mutableListOf<StallEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        StallEntity(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            address = obj.optString("address", ""),
                            notes = obj.optString("notes", ""),
                            isActive = obj.optBoolean("isActive", true),
                            photoUri = obj.optString("photoUri").takeIf { it.isNotBlank() }
                        )
                    )
                }
                if (list.isNotEmpty()) appDao.insertStalls(list)
            }

            // 2. Restore Menu Items
            if (root.has("menu_items")) {
                val arr = root.getJSONArray("menu_items")
                val list = mutableListOf<MenuItemEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        MenuItemEntity(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            hpp = obj.optDouble("hpp", 0.0),
                            price = obj.optDouble("price", 0.0),
                            unit = obj.optString("unit", "pcs"),
                            isActive = obj.optBoolean("isActive", true),
                            photoUri = obj.optString("photoUri").takeIf { it.isNotBlank() }
                        )
                    )
                }
                if (list.isNotEmpty()) appDao.insertMenuItems(list)
            }

            // 3. Restore Stall Prices
            if (root.has("stall_menu_prices")) {
                val arr = root.getJSONArray("stall_menu_prices")
                val list = mutableListOf<StallMenuPriceEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        StallMenuPriceEntity(
                            id = obj.optLong("id", 0L),
                            stallId = obj.getLong("stallId"),
                            menuId = obj.getLong("menuId"),
                            price = obj.getDouble("price")
                        )
                    )
                }
                if (list.isNotEmpty()) appDao.insertStallPrices(list)
            }

            // 4. Restore Deposits
            if (root.has("deposits")) {
                val arr = root.getJSONArray("deposits")
                val list = mutableListOf<DepositEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val sId = obj.optLong("stallId", -1L)
                    list.add(
                        DepositEntity(
                            id = obj.optLong("id", 0L),
                            stallId = if (sId > 0) sId else null,
                            stallName = obj.getString("stallName"),
                            date = obj.getString("date"),
                            notes = obj.optString("notes", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                if (list.isNotEmpty()) appDao.insertDeposits(list)
            }

            // 5. Restore Deposit Items
            if (root.has("deposit_items")) {
                val arr = root.getJSONArray("deposit_items")
                val list = mutableListOf<DepositItemEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val mId = obj.optLong("menuId", -1L)
                    list.add(
                        DepositItemEntity(
                            id = obj.optLong("id", 0L),
                            depositId = obj.getLong("depositId"),
                            menuId = if (mId > 0) mId else null,
                            menuName = obj.getString("menuName"),
                            unit = obj.optString("unit", "pcs"),
                            priceSnapshot = obj.optDouble("priceSnapshot", 0.0),
                            quantitySent = obj.optInt("quantitySent", 0),
                            quantitySold = obj.optInt("quantitySold", 0),
                            damagedQty = obj.optInt("damagedQty", 0),
                            resaleableQty = obj.optInt("resaleableQty", 0),
                            selfConsumedQty = obj.optInt("selfConsumedQty", 0)
                        )
                    )
                }
                if (list.isNotEmpty()) appDao.insertDepositItems(list)
            }

            // 6. Restore Payments
            if (root.has("payments")) {
                val arr = root.getJSONArray("payments")
                val list = mutableListOf<PaymentEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        PaymentEntity(
                            id = obj.optLong("id", 0L),
                            depositId = obj.getLong("depositId"),
                            amountPaid = if (obj.has("amountPaid")) obj.getDouble("amountPaid") else obj.optDouble("amount", 0.0),
                            paymentDate = obj.getString("paymentDate"),
                            paymentMethod = obj.optString("paymentMethod", obj.optString("method", "Tunai")),
                            notes = obj.optString("notes", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                if (list.isNotEmpty()) appDao.insertPayments(list)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun DepositAggregate.toModel(): DepositWithDetails {
        return DepositWithDetails(
            deposit = deposit,
            items = items,
            payments = payments
        )
    }
}
