package com.kelompok.duidtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kelompok.duidtracker.data.local.entity.GroupEntity
import com.kelompok.duidtracker.data.local.entity.TransactionEntity
import com.kelompok.duidtracker.data.repository.AuthRepository
import com.kelompok.duidtracker.data.repository.GroupRepository
import com.kelompok.duidtracker.data.repository.TransactionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * UI State untuk daftar grup.
 */
data class GroupListUiState(
    val groups: List<GroupEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * UI State untuk detail satu grup.
 */
data class GroupDetailUiState(
    val group: GroupEntity? = null,
    val transactions: List<TransactionEntity> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentUserId: String = ""
)

/**
 * ViewModel untuk mengelola seluruh logika fitur Grup.
 */
class GroupViewModel(
    private val groupRepo: GroupRepository,
    private val txRepo: TransactionRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _listUiState = MutableStateFlow(GroupListUiState())
    val listUiState: StateFlow<GroupListUiState> = _listUiState.asStateFlow()

    private val _detailUiState = MutableStateFlow(GroupDetailUiState())
    val detailUiState: StateFlow<GroupDetailUiState> = _detailUiState.asStateFlow()

    private var collectionJob: Job? = null

    init {
        refreshData()
    }

    /**
     * Menyegarkan data grup berdasarkan user yang aktif.
     */
    fun refreshData() {
        val userId = authRepo.getCurrentUserId()
        
        if (userId.isNullOrBlank()) {
            clearState()
            return
        }

        // Hentikan proses lama
        groupRepo.stopListener()
        collectionJob?.cancel()
        
        _detailUiState.update { it.copy(currentUserId = userId) }

        // Mulai sinkronisasi cloud real-time
        groupRepo.startFirestoreListener(userId)

        collectionJob = viewModelScope.launch {
            _listUiState.update { it.copy(isLoading = true) }
            groupRepo.getAllGroups(userId).collect { groups ->
                _listUiState.update { it.copy(groups = groups, isLoading = false) }
            }
        }
    }

    /**
     * Membersihkan state dan menghentikan sinkronisasi (saat logout).
     */
    fun clearState() {
        groupRepo.stopListener()
        collectionJob?.cancel()
        _listUiState.update { GroupListUiState() }
        _detailUiState.update { GroupDetailUiState() }
    }

    fun createGroup(name: String, description: String, budget: Double, icon: String) {
        viewModelScope.launch {
            _listUiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = groupRepo.createGroup(name, description, budget, icon)
            if (result.isFailure) {
                _listUiState.update { 
                    it.copy(errorMessage = result.exceptionOrNull()?.message, isLoading = false) 
                }
            } else {
                _listUiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun joinGroup(inviteCode: String) {
        viewModelScope.launch {
            _listUiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = groupRepo.joinGroupByCode(inviteCode)
            if (result.isFailure) {
                _listUiState.update { 
                    it.copy(errorMessage = result.exceptionOrNull()?.message, isLoading = false) 
                }
            } else {
                _listUiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadGroupDetail(groupId: String) {
        viewModelScope.launch {
            _detailUiState.update { it.copy(isLoading = true, errorMessage = null) }
            combine(
                groupRepo.getGroupById(groupId),
                txRepo.getGroupTransactions(groupId)
            ) { group, transactions ->
                val income = transactions.filter { it.type == TransactionEntity.TYPE_INCOME }.sumOf { it.nominal }
                val expense = transactions.filter { it.type == TransactionEntity.TYPE_EXPENSE }.sumOf { it.nominal }
                
                _detailUiState.update { 
                    it.copy(
                        group = group,
                        transactions = transactions,
                        totalIncome = income,
                        totalExpense = expense,
                        balance = income - expense,
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    fun addGroupTransaction(groupId: String, nama: String, nominal: Double, kategori: String, type: String, tanggal: Long) {
        val userId = authRepo.getCurrentUserId() ?: return
        val transaction = TransactionEntity(
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
        
        viewModelScope.launch {
            _detailUiState.update { it.copy(errorMessage = null) }
            val result = txRepo.addTransaction(transaction)
            if (result.isFailure) {
                _detailUiState.update { it.copy(errorMessage = "Gagal menambah transaksi: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun deleteGroupTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            _detailUiState.update { it.copy(errorMessage = null) }
            val result = txRepo.deleteTransaction(transaction)
            if (result.isFailure) {
                _detailUiState.update { it.copy(errorMessage = "Gagal menghapus: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun leaveGroup(groupId: String) {
        viewModelScope.launch {
            groupRepo.leaveGroup(groupId)
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            groupRepo.deleteGroup(groupId)
        }
    }

    fun resetBudget(groupId: String, newBudget: Double) {
        viewModelScope.launch {
            groupRepo.resetBudget(groupId, newBudget)
        }
    }

    fun clearError() {
        _detailUiState.update { it.copy(errorMessage = null) }
        _listUiState.update { it.copy(errorMessage = null) }
    }

    fun calculateSplit(total: Double, people: Int): Double {
        return if (people > 0) total / people else 0.0
    }

    class Factory(
        private val groupRepo: GroupRepository,
        private val txRepo: TransactionRepository,
        private val authRepo: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GroupViewModel(groupRepo, txRepo, authRepo) as T
        }
    }
}
