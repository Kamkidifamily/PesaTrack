package com.kamaujames.pesatrack.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kamaujames.pesatrack.data.local.TransactionEntity
import com.kamaujames.pesatrack.ui.common.Categories
import com.kamaujames.pesatrack.ui.theme.CategoryStyle
import com.kamaujames.pesatrack.ui.theme.categoryStyle
import com.kamaujames.pesatrack.ui.theme.pesa
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

// =============================================================================
// Animation
// =============================================================================

/** Flips to true right after the first frame, so values can animate in from zero. */
@Composable
fun rememberAnimationStarted(): Boolean {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    return started
}

// =============================================================================
// Surfaces and text
// =============================================================================

/** Flat card: one hairline border, no shadow. */
@Composable
fun PesaCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = content
    )
}

/** Money text with tabular figures so digits line up down a list. */
@Composable
fun AmountText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = LocalTextStyle.current.merge(TextStyle(fontFeatureSettings = "tnum"))
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)

        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun MonthChip(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(16.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            message,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// =============================================================================
// Buttons, fields, controls
// =============================================================================

@Composable
fun PesaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun pesaFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
    cursorColor = MaterialTheme.colorScheme.primary
)

@Composable
fun PesaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    prefix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        label = { Text(label) },
        placeholder = if (placeholder != null) {
            { Text(placeholder) }
        } else null,
        prefix = if (prefix != null) {
            { Text(prefix) }
        } else null,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = pesaFieldColors()
    )
}

/** Pill-shaped tab switch, like All / Income / Expense. */
@Composable
fun PesaSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(scheme.surface)
            .border(1.dp, scheme.outlineVariant, CircleShape)
            .padding(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val fill by animateColorAsState(
                targetValue = if (selected) scheme.primary else Color.Transparent,
                label = "segment"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(fill)
                    .clickable { onSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun PesaProgressBar(
    progress: Float,
    colour: Color,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp
) {
    val started = rememberAnimationStarted()
    val animated by animateFloatAsState(
        targetValue = if (started) progress.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(700),
        label = "progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .clip(CircleShape)
                .background(colour)
        )
    }
}

// =============================================================================
// Category badge and transaction row
// =============================================================================

@Composable
fun CategoryBadge(
    style: CategoryStyle,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(style.colour),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = style.icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

@Composable
fun TransactionListItem(
    transaction: TransactionEntity,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val isIncome = transaction.type == "INCOME"
    val rowModifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier

    Row(
        modifier = rowModifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryBadge(categoryStyle(transaction.category))

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(12.dp))

        AmountText(
            text = formatKesSigned(transaction.amount, isIncome),
            color = if (isIncome) MaterialTheme.pesa.income else MaterialTheme.pesa.expense,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// =============================================================================
// Donut chart
// =============================================================================

data class DonutSegment(
    val value: Double,
    val colour: Color
)

@Composable
fun DonutChart(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 18.dp,
    selectedIndex: Int? = null,
    centre: @Composable () -> Unit = {}
) {
    val started = rememberAnimationStarted()
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(1000),
        label = "donut"
    )
    val track = MaterialTheme.colorScheme.surfaceVariant
    val total = segments.sumOf { it.value }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val extra = 6.dp.toPx()
            val inset = (stroke + extra) / 2f
            val diameter = size.minDimension - inset * 2f
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = track,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke)
            )

            if (total > 0.0) {
                val gap = if (segments.size > 1) 3f else 0f
                val revealed = 360f * progress
                var cursor = 0f

                segments.forEachIndexed { index, segment ->
                    val full = (segment.value / total).toFloat() * 360f
                    val visible = (revealed - cursor).coerceIn(0f, full)
                    val drawn = (visible - gap).coerceAtLeast(0f)
                    val isSelected = selectedIndex == index
                    val dimmed = selectedIndex != null && !isSelected

                    if (drawn > 0f) {
                        drawArc(
                            color = segment.colour.copy(alpha = if (dimmed) 0.3f else 1f),
                            startAngle = -90f + cursor + gap / 2f,
                            sweepAngle = drawn,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = if (isSelected) stroke + extra else stroke)
                        )
                    }
                    cursor += full
                }
            }
        }

        centre()
    }
}

// =============================================================================
// Formatting helpers
// =============================================================================

/** "KSh 1,250" for whole amounts, "KSh 1,250.50" otherwise. Negatives get a proper minus. */
fun formatKes(amount: Double): String {
    val whole = amount % 1.0 == 0.0
    val pattern = if (whole) "%,.0f" else "%,.2f"
    val sign = if (amount < 0) "\u2212" else ""
    return sign + "KSh " + String.format(Locale.getDefault(), pattern, abs(amount))
}

/** Always rounds to whole shillings. */
fun formatKesWhole(amount: Double): String {
    val sign = if (amount < 0) "\u2212" else ""
    return sign + "KSh " + String.format(Locale.getDefault(), "%,.0f", abs(amount))
}

/** Short form for tight spots, e.g. the middle of a chart. */
fun formatKesCompact(amount: Double): String = when {
    abs(amount) >= 1_000_000 -> "KSh " + String.format(Locale.getDefault(), "%.1fM", amount / 1_000_000)
    abs(amount) >= 100_000 -> "KSh " + String.format(Locale.getDefault(), "%,.0fK", amount / 1_000)
    else -> formatKesWhole(amount)
}

/** "+KSh 500" for income, "\u2212KSh 500" for an expense. */
fun formatKesSigned(amount: Double, isIncome: Boolean): String =
    (if (isIncome) "+" else "\u2212") + formatKes(abs(amount))

/** Sign follows the value: "+KSh 500" or "\u2212KSh 500". */
fun formatNet(amount: Double): String =
    (if (amount < 0) "\u2212" else "+") + formatKes(abs(amount))

fun toLocalDate(epochMillis: Long): LocalDate =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()

fun timeLabel(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))

fun fullDateLabel(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.getDefault()))

fun dayLabel(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(
            DateTimeFormatter.ofPattern(
                if (date.year == today.year) "d MMM" else "d MMM yyyy",
                Locale.getDefault()
            )
        )
    }
}

/** "Boda Boda · Today, 10:30 AM" (or without the day when it is already shown above). */
fun transactionSubtitle(transaction: TransactionEntity, includeDay: Boolean): String {
    val label = Categories.subcategoryOf(transaction.category)
        ?: Categories.parentOf(transaction.category)
    val time = timeLabel(transaction.date)

    return if (includeDay) {
        "$label \u00B7 ${dayLabel(toLocalDate(transaction.date))}, $time"
    } else {
        "$label \u00B7 $time"
    }
}