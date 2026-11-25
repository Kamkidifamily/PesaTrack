package com.kamaujames.pesatrack.ui.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kamaujames.pesatrack.ui.common.Categories
import com.kamaujames.pesatrack.viewmodel.FinanceViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private val AnalyticsGreen = Color(0xFF174D38)
private val AnalyticsDeep = Color(0xFF0E3325)
private val AnalyticsIncome = Color(0xFF299D70)
private val AnalyticsMint = Color(0xFF7FE0B5)
private val AnalyticsExpense = Color(0xFFE76F51)
private val AnalyticsPaper = Color(0xFFEAF2ED)

private val SlicePalette = listOf(
    Color(0xFF174D38),
    Color(0xFFE76F51),
    Color(0xFFE9A23B),
    Color(0xFF4A7C9B),
    Color(0xFF299D70),
    Color(0xFF8E6BBF),
    Color(0xFFB5838D),
    Color(0xFF2A9D8F),
    Color(0xFFD4A373),
    Color(0xFF6D6875),
    Color(0xFFA3B18A)
)

private data class AnalyticsSlice(
    val name: String,
    val total: Double,
    val colour: Color,
    val subcategories: List<Pair<String, Double>>
)

private data class MonthBar(
    val label: String,
    val income: Double,
    val expenses: Double,
    val isCurrent: Boolean
)

private data class Insight(
    val emoji: String,
    val text: String
)

@Composable
fun AnalyticsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val monthKey by viewModel.monthKey.collectAsState()

    val currentMonth = YearMonth.parse(monthKey)
    val daysInMonth = currentMonth.lengthOfMonth()
    val monthLabel = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
            " " + currentMonth.year
    val shortMonth = currentMonth.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())

    // ---- This month's numbers ------------------------------------------------

    val monthlyTransactions = transactions.filter {
        YearMonth.from(toLocalDate(it.date)) == currentMonth
    }

    val income = monthlyTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val expenseTransactions = monthlyTransactions.filter { it.type == "EXPENSE" }
    val expenses = expenseTransactions.sumOf { it.amount }
    val cashFlow = income - expenses
    val savingsRate: Double? = if (income > 0) cashFlow / income * 100 else null

    // Spending grouped by MAIN category, with the subcategories kept inside.
    val slices = expenseTransactions
        .groupBy { Categories.parentOf(it.category) }
        .map { (parent, records) ->
            AnalyticsSlice(
                name = parent,
                total = records.sumOf { it.amount },
                colour = Color.Unspecified,
                subcategories = records
                    .groupBy { Categories.subcategoryOf(it.category) ?: "General" }
                    .map { (sub, subRecords) -> sub to subRecords.sumOf { it.amount } }
                    .sortedByDescending { it.second }
            )
        }
        .sortedByDescending { it.total }
        .mapIndexed { index, slice ->
            slice.copy(colour = SlicePalette[index % SlicePalette.size])
        }
    val topSlice = slices.firstOrNull()

    // Spending per day of the month (index = day number).
    val dailySpend = DoubleArray(daysInMonth + 1)
    expenseTransactions.forEach {
        dailySpend[toLocalDate(it.date).dayOfMonth] += it.amount
    }
    val peakDay = (1..daysInMonth)
        .maxByOrNull { dailySpend[it] }
        ?.takeIf { dailySpend[it] > 0.0 }

    val today = LocalDate.now()
    val isCurrentMonth = YearMonth.from(today) == currentMonth
    val daysElapsed = if (isCurrentMonth) today.dayOfMonth else daysInMonth
    val averageDaily = expenses / daysElapsed.coerceAtLeast(1)
    val projected = if (isCurrentMonth) averageDaily * daysInMonth else expenses

    val budgetsOnTrack = budgets.count { budget ->
        val spent = expenseTransactions
            .filter { Categories.parentOf(it.category).equals(budget.category, ignoreCase = true) }
            .sumOf { it.amount }
        spent <= budget.monthlyLimit
    }

    // ---- Last six months -------------------------------------------------------

    val trend = (5 downTo 0).map { monthsBack ->
        val month = currentMonth.minusMonths(monthsBack.toLong())
        val inMonth = transactions.filter { YearMonth.from(toLocalDate(it.date)) == month }
        MonthBar(
            label = month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            income = inMonth.filter { it.type == "INCOME" }.sumOf { it.amount },
            expenses = inMonth.filter { it.type == "EXPENSE" }.sumOf { it.amount },
            isCurrent = monthsBack == 0
        )
    }
    val lastMonthExpenses = trend[trend.size - 2].expenses
    val lastMonthName = currentMonth.minusMonths(1).month
        .getDisplayName(TextStyle.FULL, Locale.getDefault())

    // ---- Insights ----------------------------------------------------------------

    val insights = buildList<Insight> {
        if (expenses > 0 && lastMonthExpenses > 0) {
            val change = (expenses - lastMonthExpenses) / lastMonthExpenses * 100
            val lower = change <= 0
            add(
                Insight(
                    if (lower) "📉" else "📈",
                    "You've spent ${abs(change).roundToInt()}% ${if (lower) "less" else "more"} than in $lastMonthName."
                )
            )
        }
        if (topSlice != null && expenses > 0) {
            val share = (topSlice.total / expenses * 100).roundToInt()
            add(Insight("👀", "${topSlice.name} is your biggest category at $share% of your spending."))
        }
        if (savingsRate != null) {
            val rate = savingsRate.roundToInt()
            when {
                savingsRate >= 20 ->
                    add(Insight("", "You're keeping $rate% of your income. Great discipline!"))
                savingsRate >= 0 ->
                    add(Insight("🌱", "You're keeping $rate% of your income. Aim for 20% if you can."))
                else ->
                    add(Insight("⚠️", "You've spent ${formatShortKes(-cashFlow)} more than you earned this month."))
            }
        } else if (expenses > 0) {
            add(Insight("ℹ️", "No income recorded yet this month."))
        }
        if (peakDay != null) {
            add(Insight("🔥", "Your priciest day was $shortMonth $peakDay, with ${formatShortKes(dailySpend[peakDay])} spent."))
        }
        if (isEmpty()) {
            add(Insight("✨", "Add a few transactions and your insights will show up here."))
        }
    }

    // ---- Screen ------------------------------------------------------------------

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column {
                Text(
                    "Financial Analytics",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = AnalyticsGreen
                )
                Text(monthLabel, color = Color.Gray)
            }
        }

        item {
            CashFlowHero(
                income = income,
                expenses = expenses,
                cashFlow = cashFlow,
                savingsRate = savingsRate
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle("Insights")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(insights) { insight ->
                        InsightCard(insight)
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatTile(
                        emoji = "🗓",
                        label = "Daily average",
                        value = formatShortKes(averageDaily),
                        caption = "spent per day",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        emoji = "℗ ",
                        label = if (isCurrentMonth) "Month-end projection" else "Total spent",
                        value = formatShortKes(projected),
                        caption = if (isCurrentMonth) "at today's pace" else "for the month",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatTile(
                        emoji = "📈 ",
                        label = "Priciest day",
                        value = if (peakDay != null) "$shortMonth $peakDay" else "–",
                        caption = if (peakDay != null) formatShortKes(dailySpend[peakDay]) else "no spending yet",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        emoji = "🎯 ",
                        label = "Budgets on track",
                        value = if (budgets.isEmpty()) "–" else "$budgetsOnTrack / ${budgets.size}",
                        caption = if (budgets.isEmpty()) "no budgets set" else "within their limit",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            SpendingBreakdownCard(slices = slices, total = expenses)
        }

        item {
            TrendCard(bars = trend)
        }

        item {
            SpendingCalendarCard(
                month = currentMonth,
                dailySpend = dailySpend,
                peakDay = peakDay
            )
        }
    }
}

// =============================================================================
// Hero
// =============================================================================

@Composable
private fun CashFlowHero(
    income: Double,
    expenses: Double,
    cashFlow: Double,
    savingsRate: Double?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(AnalyticsGreen, AnalyticsDeep)))
                .padding(22.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Net cash flow", color = Color.White.copy(alpha = 0.75f))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        formatAnalyticsKes(cashFlow),
                        color = Color.White,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (cashFlow >= 0) "You're ahead this month" else "You've spent more than you earned",
                        color = if (cashFlow >= 0) AnalyticsMint else Color(0xFFFFB4A2),
                        fontSize = 13.sp
                    )
                }
                SavingsRing(rate = savingsRate)
            }

            Spacer(Modifier.height(20.dp))

            FlowBar(income = income, expenses = expenses)

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FlowLegend("Income", income, AnalyticsMint, Alignment.Start)
                FlowLegend("Expenses", expenses, AnalyticsExpense, Alignment.End)
            }
        }
    }
}

@Composable
private fun SavingsRing(rate: Double?) {
    val started = rememberAnimationStarted()
    val target = ((rate ?: 0.0) / 100.0).toFloat().coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = if (started) target else 0f,
        animationSpec = tween(1000),
        label = "savingsRing"
    )

    Box(
        modifier = Modifier.size(88.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 9.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            drawArc(
                color = Color.White.copy(alpha = 0.18f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = AnalyticsMint,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (rate == null) "–" else "${rate.roundToInt()}%",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
            Text("saved", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
        }
    }
}

@Composable
private fun FlowBar(income: Double, expenses: Double) {
    val barShape = RoundedCornerShape(50)

    if (income + expenses <= 0.0) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(barShape)
                .background(Color.White.copy(alpha = 0.18f))
        )
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(barShape)
    ) {
        Box(
            modifier = Modifier
                .weight(income.toFloat().coerceAtLeast(0.001f))
                .fillMaxHeight()
                .background(AnalyticsMint)
        )
        Box(
            modifier = Modifier
                .weight(expenses.toFloat().coerceAtLeast(0.001f))
                .fillMaxHeight()
                .background(AnalyticsExpense)
        )
    }
}

@Composable
private fun FlowLegend(
    label: String,
    amount: Double,
    colour: Color,
    alignment: Alignment.Horizontal
) {
    Column(horizontalAlignment = alignment) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(colour)
            )
            Spacer(Modifier.width(6.dp))
            Text(label, color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
        }
        Text(
            formatShortKes(amount),
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// =============================================================================
// Insights and quick stats
// =============================================================================

@Composable
private fun InsightCard(insight: Insight) {
    Card(
        modifier = Modifier
            .width(250.dp)
            .height(104.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AnalyticsPaper)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(insight.emoji, fontSize = 28.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                insight.text,
                color = AnalyticsGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatTile(
    emoji: String,
    label: String,
    value: String,
    caption: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(emoji, fontSize = 20.sp)
            Column {
                Text(label, color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    value,
                    color = AnalyticsGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(caption, color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// =============================================================================
// Spending breakdown: donut + expandable categories
// =============================================================================

@Composable
private fun SpendingBreakdownCard(
    slices: List<AnalyticsSlice>,
    total: Double
) {
    var selected by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle("Where your money went")

            if (slices.isEmpty() || total <= 0.0) {
                Spacer(Modifier.height(10.dp))
                Text("No expenses recorded for this month.", color = Color.Gray)
                return@Column
            }

            Text(
                "Tap a category to see what's inside it.",
                color = Color.Gray,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                DonutChart(slices = slices, total = total, selected = selected)
            }

            Spacer(Modifier.height(10.dp))

            slices.forEach { slice ->
                CategoryRow(
                    slice = slice,
                    total = total,
                    expanded = selected == slice.name,
                    onClick = {
                        selected = if (selected == slice.name) null else slice.name
                    }
                )
            }
        }
    }
}

@Composable
private fun DonutChart(
    slices: List<AnalyticsSlice>,
    total: Double,
    selected: String?
) {
    val started = rememberAnimationStarted()
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(1100),
        label = "donut"
    )
    val selectedSlice = slices.firstOrNull { it.name == selected }

    Box(
        modifier = Modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val baseWidth = 30.dp.toPx()
            val extra = 10.dp.toPx()
            val inset = (baseWidth + extra) / 2
            val diameter = size.minDimension - inset * 2
            val gap = 2.5f
            val revealed = 360f * progress

            var cursor = 0f
            slices.forEach { slice ->
                val full = (slice.total / total).toFloat() * 360f
                val visible = (revealed - cursor).coerceIn(0f, full)
                val drawn = (visible - gap).coerceAtLeast(0f)
                val isSelected = selected == slice.name
                val dimmed = selected != null && !isSelected

                if (drawn > 0f) {
                    drawArc(
                        color = slice.colour.copy(alpha = if (dimmed) 0.3f else 1f),
                        startAngle = -90f + cursor + gap / 2f,
                        sweepAngle = drawn,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(diameter, diameter),
                        style = Stroke(width = if (isSelected) baseWidth + extra else baseWidth)
                    )
                }
                cursor += full
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                selectedSlice?.name ?: "Total spent",
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                formatShortKes(selectedSlice?.total ?: total),
                color = AnalyticsGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 1
            )
            if (selectedSlice != null) {
                Text(
                    percentLabel(selectedSlice.total, total),
                    color = selectedSlice.colour,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(
    slice: AnalyticsSlice,
    total: Double,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(slice.colour)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                slice.name,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(formatShortKes(slice.total), fontWeight = FontWeight.Bold)
                Text(percentLabel(slice.total, total), color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(Modifier.width(8.dp))
            Text(if (expanded) "▲" else "▼", color = Color.Gray, fontSize = 10.sp)
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 22.dp, top = 10.dp)) {
                slice.subcategories.forEach { (sub, amount) ->
                    val share = if (slice.total > 0) (amount / slice.total).toFloat() else 0f

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(sub, modifier = Modifier.weight(1f), color = Color.DarkGray, fontSize = 13.sp)
                        Text(formatShortKes(amount), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { share.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(50)),
                        color = slice.colour,
                        trackColor = Color.LightGray.copy(alpha = 0.35f)
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

// =============================================================================
// Six month trend
// =============================================================================

@Composable
private fun TrendCard(bars: List<MonthBar>) {
    val started = rememberAnimationStarted()
    val maxValue = bars.maxOf { maxOf(it.income, it.expenses) }.coerceAtLeast(1.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle("Last 6 months")

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TrendLegend("Income", AnalyticsIncome)
                TrendLegend("Expenses", AnalyticsExpense)
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                bars.forEach { bar ->
                    TrendBarGroup(
                        bar = bar,
                        maxValue = maxValue,
                        started = started,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendLegend(label: String, colour: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(colour)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
private fun TrendBarGroup(
    bar: MonthBar,
    maxValue: Double,
    started: Boolean,
    modifier: Modifier = Modifier
) {
    val incomeFraction by animateFloatAsState(
        targetValue = if (started) (bar.income / maxValue).toFloat() else 0f,
        animationSpec = tween(900),
        label = "incomeBar"
    )
    val expenseFraction by animateFloatAsState(
        targetValue = if (started) (bar.expenses / maxValue).toFloat() else 0f,
        animationSpec = tween(900),
        label = "expenseBar"
    )

    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .fillMaxHeight(incomeFraction.coerceIn(0.015f, 1f))
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(AnalyticsIncome)
            )
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .fillMaxHeight(expenseFraction.coerceIn(0.015f, 1f))
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(AnalyticsExpense)
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            bar.label,
            fontSize = 12.sp,
            color = if (bar.isCurrent) AnalyticsGreen else Color.Gray,
            fontWeight = if (bar.isCurrent) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// =============================================================================
// Spending calendar (heatmap)
// =============================================================================

@Composable
private fun SpendingCalendarCard(
    month: YearMonth,
    dailySpend: DoubleArray,
    peakDay: Int?
) {
    val daysInMonth = month.lengthOfMonth()
    val maxSpend = dailySpend.maxOrNull() ?: 0.0
    val firstOffset = month.atDay(1).dayOfWeek.value - 1 // Monday = 0
    val rows = (firstOffset + daysInMonth + 6) / 7

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle("Spending calendar")
            Text(
                "Darker days mean heavier spending. The outlined day is your priciest.",
                color = Color.Gray,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { letter ->
                    Text(
                        letter,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            for (row in 0 until rows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (column in 0 until 7) {
                        val day = row * 7 + column - firstOffset + 1
                        if (day in 1..daysInMonth) {
                            CalendarCell(
                                day = day,
                                spend = dailySpend[day],
                                maxSpend = maxSpend,
                                isPeak = day == peakDay,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarCell(
    day: Int,
    spend: Double,
    maxSpend: Double,
    isPeak: Boolean,
    modifier: Modifier = Modifier
) {
    val intensity = if (maxSpend > 0 && spend > 0) (spend / maxSpend).toFloat() else 0f
    val fill = if (spend <= 0.0) {
        Color(0xFFF1F3F1)
    } else {
        AnalyticsExpense.copy(alpha = 0.2f + 0.8f * intensity)
    }
    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(fill)
            .then(if (isPeak) Modifier.border(2.dp, AnalyticsGreen, shape) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "$day",
            fontSize = 12.sp,
            fontWeight = if (isPeak) FontWeight.Bold else FontWeight.Normal,
            color = if (intensity > 0.55f) Color.White else Color(0xFF2B2B2B)
        )
    }
}

// =============================================================================
// Small helpers
// =============================================================================

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        color = AnalyticsGreen
    )
}

/** Flips to true right after the first frame so values can animate in from zero. */
@Composable
private fun rememberAnimationStarted(): Boolean {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    return started
}

private fun toLocalDate(epochMillis: Long): LocalDate =
    Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

private fun percentLabel(part: Double, total: Double): String {
    if (total <= 0.0) return "0%"
    val percent = part / total * 100
    return if (percent < 1.0) "<1%" else "${percent.roundToInt()}%"
}

private fun formatAnalyticsKes(amount: Double): String {
    return "KSh " + String.format(Locale.getDefault(), "%,.2f", amount)
}

private fun formatShortKes(amount: Double): String {
    return "KSh " + String.format(Locale.getDefault(), "%,.0f", amount)
}