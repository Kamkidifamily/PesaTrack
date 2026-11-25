package com.kamaujames.pesatrack.ui.budgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kamaujames.pesatrack.data.local.BudgetEntity
import com.kamaujames.pesatrack.ui.common.Categories
import com.kamaujames.pesatrack.ui.common.CategoryDropdown
import com.kamaujames.pesatrack.viewmodel.FinanceViewModel
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale

private val BudgetGreen = Color(0xFF174D38)
private val BudgetOrange = Color(0xFFE9A23B)
private val BudgetRed = Color(0xFFE76F51)

// Budgets are set on the main category only (expenses only).
private val budgetCategories = Categories.expense.map { it.name }

@Composable
fun BudgetsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val budgets by viewModel.budgets.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val monthKey by viewModel.monthKey.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    val currentMonth = YearMonth.parse(monthKey)

    val monthlyExpenses = transactions.filter { transaction ->
        transaction.type == "EXPENSE" &&
                Instant.ofEpochMilli(transaction.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .let { YearMonth.from(it) == currentMonth }
    }

    val totalBudget = budgets.sumOf { it.monthlyLimit }
    val totalSpent = monthlyExpenses.sumOf { it.amount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    "My Budgets",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = BudgetGreen
                )
                Text(
                    currentMonth.month.name.lowercase()
                        .replaceFirstChar { it.uppercase() } +
                            " ${currentMonth.year}",
                    color = Color.Gray
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BudgetGreen
                )
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Total monthly budget", color = Color.White.copy(alpha = 0.8f))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        formatBudgetKes(totalBudget),
                        color = Color.White,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Spent: ${formatBudgetKes(totalSpent)}",
                        color = Color.White
                    )
                    Text(
                        "Remaining: ${formatBudgetKes(totalBudget - totalSpent)}",
                        color = Color.White
                    )
                }
            }
        }

        item {
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create monthly budget")
            }
        }

        item {
            Text(
                "Category budgets",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = BudgetGreen
            )
        }

        if (budgets.isEmpty()) {
            item {
                Text(
                    "You haven't created any budgets for this month.",
                    color = Color.Gray
                )
            }
        }

        items(budgets, key = { it.id }) { budget ->
            // "Transport > Boda Boda" and "Transport > Matatu" both count
            // towards the "Transport" budget.
            val spent = monthlyExpenses
                .filter {
                    Categories.parentOf(it.category)
                        .equals(budget.category, ignoreCase = true)
                }
                .sumOf { it.amount }

            BudgetCard(
                budget = budget,
                spent = spent,
                onDelete = { viewModel.deleteBudget(budget) }
            )
        }
    }

    if (showDialog) {
        AddBudgetDialog(
            onDismiss = { showDialog = false },
            onSave = { category, amount ->
                viewModel.saveBudget(category, amount)
                showDialog = false
            }
        )
    }
}

@Composable
private fun BudgetCard(
    budget: BudgetEntity,
    spent: Double,
    onDelete: () -> Unit
) {
    val progress = (spent / budget.monthlyLimit).toFloat()
        .coerceIn(0f, 1f)

    val remaining = budget.monthlyLimit - spent

    val progressColour = when {
        spent > budget.monthlyLimit -> BudgetRed
        progress >= 0.8f -> BudgetOrange
        else -> BudgetGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(Modifier.padding(17.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    budget.category,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    formatBudgetKes(budget.monthlyLimit),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = progressColour,
                trackColor = Color.LightGray.copy(alpha = 0.4f)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Spent: ${formatBudgetKes(spent)}",
                color = Color.Gray,
                fontSize = 13.sp
            )

            Text(
                if (remaining >= 0) {
                    "Remaining: ${formatBudgetKes(remaining)}"
                } else {
                    "Over budget: ${formatBudgetKes(-remaining)}"
                },
                color = if (remaining >= 0) BudgetGreen else BudgetRed,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )

            TextButton(onClick = onDelete) {
                Text("Remove budget", color = BudgetRed)
            }
        }
    }
}

@Composable
private fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create monthly budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryDropdown(
                    label = "Category",
                    options = budgetCategories,
                    selected = selectedCategory,
                    onSelected = { selectedCategory = it }
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monthly limit (KSh)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error.isNotBlank()) {
                    Text(error, color = BudgetRed)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()

                    when {
                        selectedCategory.isBlank() -> error = "Select a category."
                        parsedAmount == null || parsedAmount <= 0 ->
                            error = "Enter a valid amount."
                        else -> onSave(selectedCategory, parsedAmount)
                    }
                }
            ) {
                Text("Save budget")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatBudgetKes(amount: Double): String {
    return "KSh " + String.format(Locale.getDefault(), "%,.2f", amount)
}