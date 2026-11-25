package com.kamaujames.pesatrack.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kamaujames.pesatrack.ui.common.Categories
import com.kamaujames.pesatrack.ui.common.CategoryDropdown
import com.kamaujames.pesatrack.ui.theme.categoryStyle
import com.kamaujames.pesatrack.ui.components.PesaButton
import com.kamaujames.pesatrack.ui.components.PesaSegmentedControl
import com.kamaujames.pesatrack.ui.components.PesaTextField
import com.kamaujames.pesatrack.ui.theme.pesa
import com.kamaujames.pesatrack.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var type by remember { mutableStateOf("EXPENSE") }
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedSubcategory by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    // Categories depend on the type, subcategories depend on the category.
    val categoryGroups = Categories.forType(type)
    val subcategories = categoryGroups
        .firstOrNull { it.name == selectedCategory }
        ?.subcategories
        .orEmpty()

    // Slide the sheet away first, then remove it.
    fun closeSheet() {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "New transaction",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            PesaSegmentedControl(
                options = listOf("Expense", "Income"),
                selectedIndex = if (type == "EXPENSE") 0 else 1,
                onSelected = { index ->
                    val newType = if (index == 0) "EXPENSE" else "INCOME"
                    if (newType != type) {
                        type = newType
                        selectedCategory = ""
                        selectedSubcategory = ""
                    }
                }
            )

            PesaTextField(
                value = title,
                onValueChange = { title = it },
                label = "Transaction name",
                placeholder = if (type == "INCOME") "e.g. November salary" else "e.g. Lunch at work"
            )

            CategoryDropdown(
                label = "Category",
                options = categoryGroups.map { it.name },
                selected = selectedCategory,
                onSelected = {
                    if (it != selectedCategory) {
                        selectedCategory = it
                        selectedSubcategory = ""
                    }
                },
                optionStyle = { categoryStyle(it) }
            )

            CategoryDropdown(
                label = "Subcategory",
                options = subcategories,
                selected = selectedSubcategory,
                onSelected = { selectedSubcategory = it },
                enabled = selectedCategory.isNotBlank()
            )

            PesaTextField(
                value = amount,
                onValueChange = { input ->
                    // Digits and a single decimal point only.
                    if (input.count { it == '.' } <= 1 && input.all { it.isDigit() || it == '.' }) {
                        amount = input
                    }
                },
                label = "Amount",
                prefix = "KSh ",
                keyboardType = KeyboardType.Decimal
            )

            PesaTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes (optional)",
                singleLine = false
            )

            if (error.isNotBlank()) {
                Text(
                    text = error,
                    color = MaterialTheme.pesa.expense,
                    fontSize = 13.sp
                )
            }

            PesaButton(
                text = "Save transaction",
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()

                    when {
                        title.isBlank() -> error = "Enter a transaction name."
                        selectedCategory.isBlank() -> error = "Select a category."
                        selectedSubcategory.isBlank() -> error = "Select a subcategory."
                        parsedAmount == null || parsedAmount <= 0 ->
                            error = "Enter a valid amount greater than zero."

                        else -> {
                            viewModel.addTransaction(
                                title = title,
                                amount = parsedAmount,
                                category = Categories.combine(
                                    selectedCategory,
                                    selectedSubcategory
                                ),
                                type = type,
                                notes = notes
                            )
                            closeSheet()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AddTransactionDialog(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    AddTransactionSheet(
        viewModel = viewModel,
        onDismiss = onDismiss
    )
}