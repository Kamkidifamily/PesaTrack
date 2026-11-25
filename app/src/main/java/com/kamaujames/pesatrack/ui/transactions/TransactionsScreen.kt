
package com.kamaujames.pesatrack.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
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
import com.kamaujames.pesatrack.data.local.TransactionEntity
import com.kamaujames.pesatrack.viewmodel.FinanceViewModel
import java.util.Locale

@Composable
fun TransactionsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier,
    onAddTransaction: () -> Unit = {}
) {
    val transactions by viewModel.transactions.collectAsState()
    var selectedTransaction by remember {
        mutableStateOf<TransactionEntity?>(null)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Transactions",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF174D38)
        )

        Text(
            text = "Your complete financial activity",
            color = Color.Gray,
            modifier = Modifier.padding(top = 5.dp, bottom = 18.dp)
        )

        if (transactions.isEmpty()) {
            Text(
                text = "No transactions recorded yet.",
                color = Color.Gray
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(transactions, key = { it.id }) { transaction ->
                    TransactionRow(
                        transaction = transaction,
                        onDelete = {
                            selectedTransaction = transaction
                        }
                    )
                }
            }
        }
    }

    selectedTransaction?.let { transaction ->
        AlertDialog(
            onDismissRequest = { selectedTransaction = null },
            title = { Text("Delete transaction?") },
            text = {
                Text("Are you sure you want to delete '${transaction.title}'?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(transaction)
                        selectedTransaction = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { selectedTransaction = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TransactionRow(
    transaction: TransactionEntity,
    onDelete: () -> Unit
) {
    val isIncome = transaction.type == "INCOME"
    val amountColour = if (isIncome) {
        Color(0xFF299D70)
    } else {
        Color(0xFFE76F51)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.title,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${transaction.category} • ${transaction.type}",
                        color = Color.Gray
                    )
                }

                Text(
                    text = "${if (isIncome) "+" else "-"} KSh ${
                        String.format(
                            Locale.getDefault(),
                            "%,.2f",
                            transaction.amount
                        )
                    }",
                    color = amountColour,
                    fontWeight = FontWeight.Bold
                )
            }

            if (transaction.notes.isNotBlank()) {
                Text(
                    text = transaction.notes,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Delete transaction")
            }
        }
    }
}
