package com.kamaujames.pesatrack.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.kamaujames.pesatrack.ui.common.Categories

data class CategoryStyle(
    val icon: ImageVector,
    val colour: Color
)

private val Slate = Color(0xFF7D8A94)

private val Housing = CategoryStyle(Icons.Rounded.Home, Color(0xFFEFA92E))
private val Food = CategoryStyle(Icons.Rounded.Restaurant, Color(0xFFF08A3C))
private val Transport = CategoryStyle(Icons.Rounded.DirectionsCar, Color(0xFF3B82F6))
private val Communication = CategoryStyle(Icons.Rounded.PhoneAndroid, Color(0xFF06A9C4))
private val Health = CategoryStyle(Icons.Rounded.LocalHospital, Color(0xFFEF4468))
private val Shopping = CategoryStyle(Icons.Rounded.ShoppingBag, Color(0xFF8B5CF6))
private val Education = CategoryStyle(Icons.Rounded.School, Color(0xFF6366F1))
private val Savings = CategoryStyle(Icons.Rounded.Savings, Color(0xFF22A861))
private val Family = CategoryStyle(Icons.Rounded.Groups, Color(0xFFE0629F))
private val Debt = CategoryStyle(Icons.Rounded.CreditCard, Color(0xFFD9443F))
private val Misc = CategoryStyle(Icons.Rounded.MoreHoriz, Slate)

private val Salary = CategoryStyle(Icons.Rounded.Payments, Color(0xFF22A861))
private val Freelance = CategoryStyle(Icons.Rounded.Work, Color(0xFF14A38F))
private val Business = CategoryStyle(Icons.Rounded.Storefront, Color(0xFFEFA92E))
private val Investments = CategoryStyle(Icons.Rounded.Insights, Color(0xFF3B82F6))
private val OtherIncome = CategoryStyle(Icons.Rounded.CardGiftcard, Color(0xFF8B5CF6))

private val styles: Map<String, CategoryStyle> = mapOf(
    // Expense categories
    "Housing & Utilities" to Housing,
    "Food & Groceries" to Food,
    "Transport" to Transport,
    "Communication" to Communication,
    "Health & Personal Care" to Health,
    "Shopping & Lifestyle" to Shopping,
    "Education & Development" to Education,
    "Savings & Investments" to Savings,
    "Family & Giving" to Family,
    "Debt & Financial Obligations" to Debt,
    "Miscellaneous" to Misc,
    // Income categories
    "Salary" to Salary,
    "Freelance" to Freelance,
    "Business" to Business,
    "Investments" to Investments,
    "Other Income" to OtherIncome,
    // Names from the first version of the app
    "Rent" to Housing,
    "Bus Fare" to Transport,
    "Clothing" to Shopping,
    "Leisure" to Shopping,
    "Income" to Salary
)

/** True when the category is one of the known ones (so it has its own icon and colour). */
fun hasCategoryStyle(name: String): Boolean =
    styles.containsKey(Categories.parentOf(name))

/** Works with a main category ("Transport") or a saved value ("Transport > Boda Boda"). */
fun categoryStyle(name: String, fallback: Color = Slate): CategoryStyle =
    styles[Categories.parentOf(name)] ?: CategoryStyle(Icons.Rounded.Category, fallback)