package com.kelompok.duidtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kelompok.duidtracker.data.local.entity.TransactionEntity
import com.kelompok.duidtracker.data.repository.AuthRepository
import com.kelompok.duidtracker.data.repository.TransactionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Data class untuk merangkum seluruh kondisi tampilan di layar Transaksi.
 */
data class TransactionUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel untuk mengelola logika Transaksi.
 */
class TransactionViewModel(
    private val repository: TransactionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow("Semua")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private var collectionJob: Job? = null

    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        _uiState,
        _selectedFilter
    ) { state, filter ->
        when (filter) {
            "Pemasukan" -> state.transactions.filter { it.type == TransactionEntity.TYPE_INCOME }
            "Pengeluaran" -> state.transactions.filter { it.type == TransactionEntity.TYPE_EXPENSE }
            else -> state.transactions
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        refreshData()
    }

    /**
     * Menyegarkan aliran data berdasarkan user yang sedang aktif.
     */
    fun refreshData() {
        val userId = authRepository.getCurrentUserId()
        
        // Safety guard: Jangan jalankan jika UID null atau kosong (Task 1)
        if (userId.isNullOrBlank()) {
            clearState()
            return
        }

        // Hentikan listener lama
        repository.stopListener()
        collectionJob?.cancel()

        // Mulai listener baru
        repository.startFirestoreListener(userId)

        collectionJob = viewModelScope.launch {
            launch {
                repository.getPersonalTransactions(userId).collect { list ->
                    _uiState.update { it.copy(transactions = list) }
                }
            }
            launch {
                repository.getPersonalTotals(userId).collect { totals ->
                    _uiState.update { 
                        it.copy(
                            totalIncome = totals.first,
                            totalExpense = totals.second
                        )
                    }
                }
            }
        }
    }

    /**
     * Membersihkan state dan menghentikan sinkronisasi (saat logout) (Task 2).
     */
    fun clearState() {
        repository.stopListener()
        collectionJob?.cancel()
        _uiState.update { TransactionUiState() }
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun addTransaction(nama: String, nominal: Double, kategori: String, type: String, tanggal: Long = System.currentTimeMillis(), groupId: String? = null) {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            val newTransaction = TransactionEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                groupId = groupId,
                type = type,
                nama = nama,
                nominal = nominal,
                kategori = kategori,
                tanggal = tanggal,
                createdAt = System.currentTimeMillis()
            )

            val result = repository.addTransaction(newTransaction)
            if (result.isFailure) {
                _uiState.update { it.copy(errorMessage = "Gagal menyimpan: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            val result = repository.deleteTransaction(transaction)
            if (result.isFailure) {
                _uiState.update { 
                    it.copy(errorMessage = "Gagal menghapus: ${result.exceptionOrNull()?.message}") 
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    class Factory(
        private val repository: TransactionRepository, 
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TransactionViewModel(repository, authRepository) as T
        }
    }
}
