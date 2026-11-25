package com.kamaujames.pesatrack.ui.common

data class CategoryGroup(
    val name: String,
    val subcategories: List<String>
)

object Categories {

    // A saved transaction stores "Parent > Sub", e.g. "Transport > Boda Boda".
    // Budgets only use the parent, so every subcategory rolls up into it.
    const val SEPARATOR = " > "

    val expense = listOf(
        CategoryGroup(
            "Housing & Utilities",
            listOf("Rent", "Electricity", "Water", "Internet", "Gas", "Repairs")
        ),
        CategoryGroup(
            "Food & Groceries",
            listOf("Groceries", "Restaurant", "Takeaway", "Snacks", "Drinking Water")
        ),
        CategoryGroup(
            "Transport",
            listOf("Matatu", "Boda Boda", "Taxi", "Fuel", "Parking", "Maintenance")
        ),
        CategoryGroup(
            "Communication",
            listOf("Airtime", "Data Bundles", "Phone Bills", "Subscriptions")
        ),
        CategoryGroup(
            "Health & Personal Care",
            listOf("Medication", "Hospital", "Insurance", "Haircuts", "Toiletries")
        ),
        CategoryGroup(
            "Shopping & Lifestyle",
            listOf("Clothing", "Shoes", "Electronics", "Entertainment", "Hobbies")
        ),
        CategoryGroup(
            "Education & Development",
            listOf("Tuition", "Books", "Online Courses", "Certifications", "Software")
        ),
        CategoryGroup(
            "Savings & Investments",
            listOf("Emergency Fund", "Savings", "Money Market Fund", "Shares", "SACCO")
        ),
        CategoryGroup(
            "Family & Giving",
            listOf("Family Support", "Gifts", "Donations", "Church", "Contributions")
        ),
        CategoryGroup(
            "Debt & Financial Obligations",
            listOf("Loan Repayment", "Credit Card", "Fuliza", "Bank Charges", "Interest")
        ),
        CategoryGroup(
            "Miscellaneous",
            listOf("Unexpected Expenses", "Fines", "Other")
        )
    )

    val income = listOf(
        CategoryGroup("Salary", listOf("Monthly Salary", "Allowances", "Bonuses")),
        CategoryGroup("Freelance", listOf("Consulting", "Contract Work")),
        CategoryGroup("Business", listOf("Sales", "Business Profit")),
        CategoryGroup("Investments", listOf("Dividends", "Interest", "Returns")),
        CategoryGroup("Other Income", listOf("Gifts Received", "Refunds", "Other"))
    )

    /** Category list for a transaction type: "INCOME" or "EXPENSE". */
    fun forType(type: String): List<CategoryGroup> =
        if (type == "INCOME") income else expense

    /** Builds the value that gets saved, e.g. "Transport > Boda Boda". */
    fun combine(category: String, subcategory: String): String =
        category + SEPARATOR + subcategory

    /** "Transport > Boda Boda" -> "Transport". Old values without a subcategory are returned as they are. */
    fun parentOf(stored: String): String = stored.substringBefore(SEPARATOR)

    /** "Transport > Boda Boda" -> "Boda Boda", or null when there is no subcategory. */
    fun subcategoryOf(stored: String): String? =
        stored.substringAfter(SEPARATOR, "").ifEmpty { null }
}