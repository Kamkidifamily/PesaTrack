
package com.kamaujames.pesatrack.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kamaujames.pesatrack.data.local.TransactionEntity
import com.kamaujames.pesatrack.viewmodel.FinanceViewModel
import java.util.Locale
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import com.kamaujames.pesatrack.ui.transactions.AddTransactionDialog










import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment

private val ForestGreen = Color(0xFF174D38)
private val IncomeGreen = Color(0xFF299D70)
private val ExpenseRed = Color(0xFFE76F51)

@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier,
    onViewAllTransactions: () -> Unit = {},
    onOpenAnalytics: () -> Unit = {}
) {
    val income by viewModel.totalIncome.collectAsState()
    val expenses by viewModel.totalExpenses.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    var showAddTransaction by remember { mutableStateOf(false) }
    val balance = income - expenses

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    )



    {
        item {
            Column {
                Text(
                    text = "Welcome to",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Text(
                    text = "PesaTrack",
                    color = ForestGreen,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Take control of your money.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

        }

        item {
            Button(
                onClick = { showAddTransaction = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add a transaction")
            }
        }

        item {
            BalanceCard(balance = balance)
        }

        item {
            Text(
                text = "Financial overview",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Income",
                    amount = income,
                    colour = IncomeGreen,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "Expenses",
                    amount = expenses,
                    colour = ExpenseRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onViewAllTransactions() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent transactions",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
                )

                Text(
                    text = "${transactions.size} records >",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }




        if (transactions.isEmpty()) {
            item {
                Text(
                    text = "No transactions yet. Add your first transaction to get started.",
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(transactions.take(5), key = { it.id }) { transaction ->
                DashboardTransactionItem(transaction)
            }
        }
    }
    if (showAddTransaction) {
        AddTransactionDialog(
            viewModel = viewModel,
            onDismiss = { showAddTransaction = false }
        )
    }
}

@Composable
private fun BalanceCard(balance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = ForestGreen
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Text(
                text = "Total balance",
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = formatKes(balance),
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your recorded income minus expenses",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: Double,
    colour: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatKes(amount),
                color = colour,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DashboardTransactionItem(
    transaction: TransactionEntity
) {
    val isIncome = transaction.type == "INCOME"
    val amountColour = if (isIncome) IncomeGreen else ExpenseRed
    val sign = if (isIncome) "+" else "-"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = transaction.category,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "$sign ${formatKes(transaction.amount)}",
                color = amountColour,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatKes(amount: Double): String {
    return "KSh " + String.format(Locale.getDefault(), "%,.2f", amount)
}
