package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.DepositEntity
import com.example.data.local.entity.DepositItemEntity
import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.StallEntity
import com.example.data.local.entity.StallMenuPriceEntity
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.UserPreferences
import com.example.data.repository.AppRepository
import com.example.model.AppLanguage
import com.example.model.AppStrings
import com.example.model.DepositWithDetails
import com.example.model.TranslationHelper
import com.example.util.AppConfig
import com.example.util.FormatUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenDestination {
    SPLASH,
    BERANDA,
    SETORAN,
    PENJUALAN,
    RIWAYAT,
    MENU,
    LAPAK,
    ATUR_HARGA,
    PENGATURAN,
    TENTANG,
    DETAIL_TRANSAKSI
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = UserPreferences(application)
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = AppRepository(database.appDao())

    val language: StateFlow<AppLanguage> = preferences.language
    val themeMode: StateFlow<AppThemeMode> = preferences.themeMode
    val lastBackupTime: StateFlow<Long> = preferences.lastBackupTime

    private val _currentScreen = MutableStateFlow(ScreenDestination.SPLASH)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val _selectedDate = MutableStateFlow(FormatUtils.getTodayDateIso())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedDepositForDetail = MutableStateFlow<DepositWithDetails?>(null)
    val selectedDepositForDetail: StateFlow<DepositWithDetails?> = _selectedDepositForDetail.asStateFlow()

    private val _selectedDepositForSales = MutableStateFlow<DepositWithDetails?>(null)
    val selectedDepositForSales: StateFlow<DepositWithDetails?> = _selectedDepositForSales.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Stalls
    val allStalls: StateFlow<List<StallEntity>> = repository.allStalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeStalls: StateFlow<List<StallEntity>> = repository.activeStalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stall prices
    val allStallPrices: StateFlow<List<StallMenuPriceEntity>> = repository.allStallPrices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Menu catalogs
    val allMenuItems: StateFlow<List<MenuItemEntity>> = repository.allMenuItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeMenuItems: StateFlow<List<MenuItemEntity>> = repository.activeMenuItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All deposits with reactive details
    val allDeposits: StateFlow<List<DepositWithDetails>> = repository.allDeposits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered deposits for the selected date on Beranda
    val selectedDateDeposits: StateFlow<List<DepositWithDetails>> = combine(allDeposits, selectedDate) { deposits, date ->
        deposits.filter { it.deposit.date == date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Helper strings getter based on current language
    val strings: AppStrings
        get() = TranslationHelper.get(language.value)

    val isTrial: Boolean get() = AppConfig.IS_TRIAL

    private val _isTrialExpired = MutableStateFlow(false)
    val isTrialExpired: StateFlow<Boolean> = _isTrialExpired.asStateFlow()

    private val _trialMinutesElapsed = MutableStateFlow(0L)
    val trialMinutesElapsed: StateFlow<Long> = _trialMinutesElapsed.asStateFlow()

    fun checkTrialStatus() {
        AppConfig.initTrialTracking(getApplication())
        val elapsed = AppConfig.getElapsedMinutes(getApplication())
        _trialMinutesElapsed.value = elapsed
        _isTrialExpired.value = AppConfig.isTrialExpired(getApplication())
    }

    init {
        checkTrialStatus()
        viewModelScope.launch {
            repository.ensureInitialData()
        }
    }

    fun navigateTo(screen: ScreenDestination) {
        _currentScreen.value = screen
    }

    fun setSelectedDate(dateIso: String) {
        _selectedDate.value = dateIso
    }

    fun shiftSelectedDate(days: Int) {
        _selectedDate.value = FormatUtils.shiftDays(_selectedDate.value, days)
    }

    fun selectDepositForDetail(deposit: DepositWithDetails) {
        _selectedDepositForDetail.value = deposit
        _currentScreen.value = ScreenDestination.DETAIL_TRANSAKSI
    }

    fun selectDepositForSales(deposit: DepositWithDetails) {
        _selectedDepositForSales.value = deposit
        _currentScreen.value = ScreenDestination.PENJUALAN
    }

    fun clearDepositForSales() {
        _selectedDepositForSales.value = null
    }

    fun setLanguage(lang: AppLanguage) {
        preferences.setLanguage(lang)
    }

    fun setThemeMode(mode: AppThemeMode) {
        preferences.setThemeMode(mode)
    }

    // === Stall (Lapak) Operations ===
    fun addStall(
        name: String,
        address: String = "",
        notes: String = "",
        photoUri: String? = null,
        onCreated: ((Long) -> Unit)? = null
    ) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            if (isTrial && allStalls.value.size >= AppConfig.TRIAL_MAX_STALLS) {
                _userMessage.emit("Batas Trial: Maksimal 2 Lapak. Hubungi ${AppConfig.DEVELOPER_EMAIL} untuk Full Version.")
                return@launch
            }
            val stall = StallEntity(
                name = name.trim(),
                address = address.trim(),
                notes = notes.trim(),
                photoUri = photoUri,
                isActive = true
            )
            val newId = repository.insertStall(stall)
            // Pre-seed default prices for active menu items from their base price
            activeMenuItems.value.forEach { m ->
                repository.setStallPrice(newId, m.id, m.price)
            }
            _userMessage.emit("Lapak ${stall.name} berhasil ditambahkan")
            onCreated?.invoke(newId)
        }
    }

    fun updateStall(stall: StallEntity) {
        viewModelScope.launch {
            repository.updateStall(stall)
            _userMessage.emit("Lapak ${stall.name} berhasil diperbarui")
        }
    }

    fun updateStallNameAndPhoto(id: Long, newName: String, newPhotoUri: String?) {
        viewModelScope.launch {
            val existing = allStalls.value.find { it.id == id } ?: return@launch
            val updated = existing.copy(name = newName.trim(), photoUri = newPhotoUri)
            repository.updateStall(updated)
            _userMessage.emit("Lapak ${updated.name} berhasil diperbarui")
        }
    }

    fun toggleStallActive(stall: StallEntity) {
        viewModelScope.launch {
            repository.updateStall(stall.copy(isActive = !stall.isActive))
        }
    }

    fun deleteStall(id: Long) {
        viewModelScope.launch {
            repository.deleteStall(id)
            _userMessage.emit("Lapak dihapus")
        }
    }

    // === Stall Price Management ===
    fun setStallPrice(stallId: Long, menuId: Long, price: Double) {
        viewModelScope.launch {
            repository.setStallPrice(stallId, menuId, price)
            _userMessage.emit("Harga berhasil disimpan")
        }
    }

    suspend fun getPriceForStallAndMenu(stallId: Long, menuId: Long): Double? {
        return repository.getPriceForStallAndMenu(stallId, menuId)
    }

    // === Setoran Operations ===
    fun createDeposit(
        date: String,
        stallId: Long?,
        stallName: String,
        notes: String,
        items: List<DepositItemEntity>,
        isHoliday: Boolean = false,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (stallName.isBlank()) {
                _userMessage.emit(strings.lapakWajibDipilih)
                return@launch
            }
            if (!isHoliday && (items.isEmpty() || items.all { it.quantitySent <= 0 })) {
                _userMessage.emit(strings.setoranValidasiError)
                return@launch
            }
            if (!isHoliday) {
                // Check if any item has price <= 0
                val invalidPrice = items.find { it.quantitySent > 0 && it.priceSnapshot <= 0 }
                if (invalidPrice != null) {
                    _userMessage.emit(strings.hargaBelumDitentukan)
                    return@launch
                }
            }

            val validItems = if (isHoliday) emptyList() else items.filter { it.quantitySent > 0 }
            val deposit = DepositEntity(
                date = date,
                stallId = stallId,
                stallName = stallName.trim(), // Snapshot of stall name
                notes = notes.trim(),
                isHoliday = isHoliday
            )
            repository.createDeposit(deposit, validItems)
            if (isHoliday) {
                _userMessage.emit("Status lapak libur berhasil dicatat")
            } else {
                _userMessage.emit(strings.setoranBerhasilDisimpan)
            }
            onSuccess()
        }
    }

    fun deleteDeposit(depositId: Long) {
        viewModelScope.launch {
            repository.deleteDeposit(depositId)
            if (_selectedDepositForDetail.value?.deposit?.id == depositId) {
                _selectedDepositForDetail.value = null
                _currentScreen.value = ScreenDestination.RIWAYAT
            }
            _userMessage.emit("Setoran berhasil dihapus")
        }
    }

    // === Sales Result Operations ===
    fun saveSalesResults(
        items: List<DepositItemEntity>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val invalidItem = items.find { it.quantitySold > it.quantitySent || it.quantitySold < 0 }
            if (invalidItem != null) {
                _userMessage.emit(strings.validasiTerjualMelebihiKirim)
                return@launch
            }

            val markedItems = items.map { it.copy(hasRecordedSale = true) }
            repository.updateSalesResult(markedItems)
            _userMessage.emit(strings.penjualanBerhasilDisimpan)
            _selectedDepositForSales.value?.deposit?.id?.let { depId ->
                val updated = repository.getDepositById(depId)
                _selectedDepositForDetail.value = updated
            }
            onSuccess()
        }
    }

    // === Payment Operations ===
    fun recordPayment(
        depositId: Long,
        amount: Double,
        date: String,
        method: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (amount <= 0) return@launch
            val payment = PaymentEntity(
                depositId = depositId,
                amountPaid = amount,
                paymentDate = date,
                paymentMethod = method,
                notes = notes.trim()
            )
            repository.addPayment(payment)
            _userMessage.emit(strings.pembayaranBerhasilDisimpan)
            val updated = repository.getDepositById(depositId)
            _selectedDepositForDetail.value = updated
            onSuccess()
        }
    }

    fun deletePayment(paymentId: Long, depositId: Long) {
        viewModelScope.launch {
            repository.deletePayment(paymentId)
            val updated = repository.getDepositById(depositId)
            _selectedDepositForDetail.value = updated
        }
    }

    // === Menu Catalog Operations ===
    fun addMenuItem(name: String, price: Double, unit: String) {
        addMenuItemFull(name, 0.0, unit, price, null, emptyMap())
    }

    fun addMenuItemFull(
        name: String,
        hpp: Double,
        unit: String,
        defaultPrice: Double,
        photoUri: String? = null,
        stallPrices: Map<Long, Double>
    ) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            if (isTrial && allMenuItems.value.size >= AppConfig.TRIAL_MAX_PRODUCTS) {
                _userMessage.emit("Batas Trial: Maksimal 2 Produk. Hubungi ${AppConfig.DEVELOPER_EMAIL} untuk Full Version.")
                return@launch
            }
            val item = MenuItemEntity(
                name = name.trim(),
                hpp = hpp,
                price = defaultPrice,
                unit = if (unit.isBlank()) "pcs" else unit.trim(),
                photoUri = photoUri,
                isActive = true
            )
            val newMenuId = repository.insertMenuItem(item)
            stallPrices.forEach { (stallId, p) ->
                if (p > 0) {
                    repository.setStallPrice(stallId, newMenuId, p)
                }
            }
            _userMessage.emit("Menu ${item.name} berhasil ditambahkan")
        }
    }

    fun updateMenuItem(item: MenuItemEntity) {
        viewModelScope.launch {
            repository.updateMenuItem(item)
            _userMessage.emit("Menu ${item.name} berhasil diperbarui")
        }
    }

    fun updateMenuItemFull(
        item: MenuItemEntity,
        stallPrices: Map<Long, Double>
    ) {
        viewModelScope.launch {
            repository.updateMenuItem(item)
            stallPrices.forEach { (stallId, p) ->
                if (p > 0) {
                    repository.setStallPrice(stallId, item.id, p)
                }
            }
            _userMessage.emit("Menu ${item.name} berhasil diperbarui")
        }
    }

    fun toggleMenuItemActive(item: MenuItemEntity) {
        viewModelScope.launch {
            repository.updateMenuItem(item.copy(isActive = !item.isActive))
        }
    }

    fun deleteMenuItem(id: Long) {
        viewModelScope.launch {
            repository.deleteMenuItem(id)
            _userMessage.emit("Menu dihapus")
        }
    }

    // === Google Drive Backup & Restore Operations ===
    suspend fun exportBackupJson(): String {
        return repository.createBackupJson()
    }

    fun recordBackupSuccess() {
        preferences.setLastBackupTime(System.currentTimeMillis())
        viewModelScope.launch {
            _userMessage.emit("Backup data berhasil disimpan")
        }
    }

    fun restoreBackup(jsonString: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.restoreFromBackupJson(jsonString)
            if (success) {
                _userMessage.emit("Data berhasil dipulihkan dari backup")
            } else {
                _userMessage.emit("Gagal memulihkan data: Format file tidak valid")
            }
            onComplete(success)
        }
    }
}
