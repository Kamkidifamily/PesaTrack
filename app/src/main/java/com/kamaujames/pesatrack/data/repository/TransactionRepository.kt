
package com.kamaujames.pesatrack.data.repository

import com.kamaujames.pesatrack.data.local.BudgetDao
import com.kamaujames.pesatrack.data.local.BudgetEntity
import com.kamaujames.pesatrack.data.local.TransactionDao
import com.kamaujames.pesatrack.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {

    val allTransactions: Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions()

    val totalIncome: Flow<Double> =
        transactionDao.getTotalIncome()

    val totalExpenses: Flow<Double> =
        transactionDao.getTotalExpenses()

    fun getBudgetsForMonth(monthKey: String): Flow<List<BudgetEntity>> =
        budgetDao.getBudgetsForMonth(monthKey)

    suspend fun insert(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun delete(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun saveBudget(budget: BudgetEntity) {
        budgetDao.saveBudget(budget)
    }

    suspend fun deleteBudget(id: String) {
        budgetDao.deleteBudget(id)
    }
}
