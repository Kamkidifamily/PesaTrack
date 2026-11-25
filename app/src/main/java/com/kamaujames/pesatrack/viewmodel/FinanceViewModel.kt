
package com.kamaujames.pesatrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamaujames.pesatrack.data.local.BudgetEntity
import com.kamaujames.pesatrack.data.local.TransactionEntity
import com.kamaujames.pesatrack.data.repository.TransactionRepository
import java.time.YearMonth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map





class FinanceViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val currentMonth = MutableStateFlow(YearMonth.now())

    val monthKey: StateFlow<String> =
        currentMonth
            .map { it.toString() }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                YearMonth.now().toString()
            )

    val transactions: StateFlow<List<TransactionEntity>> =
        repository.allTransactions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val totalIncome: StateFlow<Double> =
        repository.totalIncome.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0.0
        )

    val totalExpenses: StateFlow<Double> =
        repository.totalExpenses.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0.0
        )

    val budgets: StateFlow<List<BudgetEntity>> =
        monthKey
            .flatMapLatest { key ->
                repository.getBudgetsForMonth(key)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    fun addTransaction(
        title: String,
        amount: Double,
        category: String,
        type: String,
        notes: String = ""
    ) {
        if (title.isBlank() || category.isBlank() || amount <= 0.0) return
        if (type != "INCOME" && type != "EXPENSE") return

        viewModelScope.launch {
            repository.insert(
                TransactionEntity(
                    title = title.trim(),
                    amount = amount,
                    category = category.trim(),
                    type = type,
                    notes = notes
                )
            )
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.delete(transaction)
        }
    }

    fun saveBudget(category: String, amount: Double) {
        if (category.isBlank() || amount <= 0.0) return

        val key = monthKey.value
        val cleanCategory = category.trim()

        viewModelScope.launch {
            repository.saveBudget(
                BudgetEntity(
                    id = "$cleanCategory-$key",
                    category = cleanCategory,
                    monthlyLimit = amount,
                    monthKey = key
                )
            )
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.deleteBudget(budget.id)
        }
    }
}
