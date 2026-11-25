package com.kamaujames.pesatrack.ui.dashboard

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kamaujames.pesatrack.ui.analytics.AnalyticsScreen
import com.kamaujames.pesatrack.ui.budgets.BudgetsScreen
import com.kamaujames.pesatrack.ui.transactions.AddTransactionSheet
import com.kamaujames.pesatrack.ui.transactions.TransactionsScreen
import com.kamaujames.pesatrack.viewmodel.FinanceViewModel

private const val TAB_DASHBOARD = 0
private const val TAB_TRANSACTIONS = 1
private const val TAB_BUDGETS = 2
private const val TAB_ANALYTICS = 3

@Composable
fun PesaTrackApp(viewModel: FinanceViewModel) {

    var selectedTab by rememberSaveable { mutableStateOf(TAB_DASHBOARD) }
    var showAddTransaction by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            PesaBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onAddClick = { showAddTransaction = true }
            )
        }
    ) { innerPadding ->

        Crossfade(
            targetState = selectedTab,
            modifier = Modifier.padding(innerPadding),
            label = "tab"
        ) { tab ->
            when (tab) {
                TAB_DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onViewAllTransactions = { selectedTab = TAB_TRANSACTIONS },
                    onOpenAnalytics = { selectedTab = TAB_ANALYTICS }
                )

                TAB_TRANSACTIONS -> TransactionsScreen(
                    viewModel = viewModel,
                    onAddTransaction = { showAddTransaction = true }
                )

                TAB_BUDGETS -> BudgetsScreen(viewModel = viewModel)

                else -> AnalyticsScreen(viewModel = viewModel)
            }
        }
    }

    if (showAddTransaction) {
        AddTransactionSheet(
            viewModel = viewModel,
            onDismiss = { showAddTransaction = false }
        )
    }
}

@Composable
private fun PesaBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onAddClick: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(68.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    icon = Icons.Rounded.Home,
                    label = "Dashboard",
                    selected = selectedTab == TAB_DASHBOARD,
                    onClick = { onTabSelected(TAB_DASHBOARD) },
                    modifier = Modifier.weight(1f)
                )
                NavItem(
                    icon = Icons.Rounded.Receipt,
                    label = "Transactions",
                    selected = selectedTab == TAB_TRANSACTIONS,
                    onClick = { onTabSelected(TAB_TRANSACTIONS) },
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable(role = Role.Button, onClick = onAddClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add transaction",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                NavItem(
                    icon = Icons.Rounded.PieChart,
                    label = "Budgets",
                    selected = selectedTab == TAB_BUDGETS,
                    onClick = { onTabSelected(TAB_BUDGETS) },
                    modifier = Modifier.weight(1f)
                )
                NavItem(
                    icon = Icons.Rounded.BarChart,
                    label = "Analytics",
                    selected = selectedTab == TAB_ANALYTICS,
                    onClick = { onTabSelected(TAB_ANALYTICS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            color = tint,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}