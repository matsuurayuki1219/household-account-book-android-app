package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Transaction
import com.example.data.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository
    val allTransactions: StateFlow<List<Transaction>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TransactionRepository(database.transactionDao())
        
        allTransactions = repository.allTransactions
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        
        prepopulateIfEmpty()
    }

    private fun prepopulateIfEmpty() {
        viewModelScope.launch {
            // Check if database is empty by waiting for the first value of flow
            val currentList = repository.allTransactions.first()
            if (currentList.isEmpty()) {
                val cal = Calendar.getInstance()
                
                val samples = listOf(
                    Transaction(
                        title = "給与 (5月分)",
                        amount = 320000,
                        type = "INCOME",
                        category = "給与",
                        timestamp = cal.apply { add(Calendar.DAY_OF_YEAR, -5) }.timeInMillis,
                        memo = "毎月の給与。お疲れ様でした。"
                    ),
                    Transaction(
                        title = "マンション家賃",
                        amount = 78000,
                        type = "EXPENSE",
                        category = "住宅・光熱",
                        timestamp = cal.apply { add(Calendar.DAY_OF_YEAR, 2) }.timeInMillis,
                        memo = "5月分家賃支払い完了"
                    ),
                    Transaction(
                        title = "スタバお茶代",
                        amount = 650,
                        type = "EXPENSE",
                        category = "食費",
                        timestamp = cal.apply { add(Calendar.DAY_OF_YEAR, 2) }.timeInMillis,
                        memo = "のんびり時間"
                    ),
                    Transaction(
                        title = "スーパーお買い物",
                        amount = 4320,
                        type = "EXPENSE",
                        category = "食費",
                        timestamp = System.currentTimeMillis() - 20000,
                        memo = "食材まとめ買い。お肉、野菜など"
                    ),
                    Transaction(
                        title = "週末映画鑑賞",
                        amount = 1800,
                        type = "EXPENSE",
                        category = "趣味・娯楽",
                        timestamp = cal.apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis,
                        memo = "新作SF映画"
                    ),
                    Transaction(
                        title = "ドラッグストア日用品",
                        amount = 2150,
                        type = "EXPENSE",
                        category = "日用品",
                        timestamp = cal.apply { add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis,
                        memo = "洗剤、ハブラシ等"
                    ),
                    Transaction(
                        title = "友人とのカフェディナー",
                        amount = 5500,
                        type = "EXPENSE",
                        category = "交際費",
                        timestamp = cal.apply { add(Calendar.DAY_OF_YEAR, -2) }.timeInMillis,
                        memo = "渋谷にて久々の再会"
                    ),
                    Transaction(
                        title = "地下鉄切符",
                        amount = 280,
                        type = "EXPENSE",
                        category = "交通費",
                        timestamp = System.currentTimeMillis() - 50000,
                        memo = "打ち合わせ移動"
                    )
                )
                
                samples.forEach { repository.insert(it) }
            }
        }
    }

    // Filter states
    private val _selectedTypeFilter = MutableStateFlow("ALL") // "ALL", "EXPENSE", "INCOME"
    val selectedTypeFilter: StateFlow<String> = _selectedTypeFilter.asStateFlow()

    private val _chartsTab = MutableStateFlow(0) // 0 for Donut, 1 for Bar Chart
    val chartsTab: StateFlow<Int> = _chartsTab.asStateFlow()

    fun setTypeFilter(filter: String) {
        _selectedTypeFilter.value = filter
    }

    fun setChartsTab(tab: Int) {
        _chartsTab.value = tab
    }

    fun insertTransaction(title: String, amount: Long, type: String, category: String, memo: String = "", timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            if (title.isNotBlank() && amount > 0) {
                repository.insert(
                    Transaction(
                        title = title,
                        amount = amount,
                        type = type,
                        category = category,
                        timestamp = timestamp,
                        memo = memo
                    )
                )
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.delete(transaction)
        }
    }
}

class BudgetViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
