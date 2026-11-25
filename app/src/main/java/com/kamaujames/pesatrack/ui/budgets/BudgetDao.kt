
package com.kamaujames.pesatrack.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBudget(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE monthKey = :monthKey ORDER BY category ASC")
    fun getBudgetsForMonth(monthKey: String): Flow<List<BudgetEntity>>

    @Query("DELETE FROM budgets WHERE id = :budgetId")
    suspend fun deleteBudget(budgetId: String)
}
