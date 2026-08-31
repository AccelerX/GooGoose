package com.example.googoose.data

import com.example.googoose.data.model.TxnType

/**
 * Demo data inserted once when the local database is first created — same
 * "Riverside Coffee & Goods" sample content as before, just as real rows now
 * instead of a fixed in-memory display list. Dates are anchored to the
 * project's reference "today" (2026-08-30) so date groups ("Today",
 * "Yesterday", ...) still read naturally on first launch.
 */
object SeedData {

    const val defaultBusinessName = "Riverside Coffee & Goods"
    private const val REF_YEAR = 2026
    private const val REF_MONTH = 8
    private const val REF_DAY = 30

    data class SeedTransaction(
        val name: String,
        val category: String,
        val amount: Double,
        val type: TxnType,
        val method: String,
        val paid: Boolean,
        val remarks: String,
        val daysAgo: Int,
        val hour: Int,
        val minute: Int,
    ) {
        fun occurredAt(): Long {
            val cal = java.util.Calendar.getInstance().apply {
                set(REF_YEAR, REF_MONTH - 1, REF_DAY, hour, minute, 0)
                set(java.util.Calendar.MILLISECOND, 0)
                add(java.util.Calendar.DAY_OF_MONTH, -daysAgo)
            }
            return cal.timeInMillis
        }
    }

    val transactions: List<SeedTransaction> = listOf(
        SeedTransaction(
            name = "POS batch settlement", category = "Sales", amount = 412.50, type = TxnType.INCOME,
            method = "Card", paid = true, remarks = "", daysAgo = 0, hour = 17, minute = 42,
        ),
        SeedTransaction(
            name = "Sunrise Roasters — beans", category = "Inventory & Supplies", amount = 186.40, type = TxnType.SPEND,
            method = "Bank transfer", paid = true, remarks = "Weekly bean restock, 20lb house blend.",
            daysAgo = 0, hour = 9, minute = 15,
        ),
        SeedTransaction(
            name = "POS batch settlement", category = "Sales", amount = 389.20, type = TxnType.INCOME,
            method = "Card", paid = true, remarks = "", daysAgo = 1, hour = 18, minute = 3,
        ),
        SeedTransaction(
            name = "Instagram ads", category = "Marketing", amount = 45.00, type = TxnType.SPEND,
            method = "Credit card", paid = false, remarks = "Boosted post, weekend promo.",
            daysAgo = 1, hour = 11, minute = 20,
        ),
        SeedTransaction(
            name = "Catering order — Hyde & Co.", category = "Catering & Events", amount = 540.00, type = TxnType.INCOME,
            method = "Invoice", paid = false, remarks = "Office lunch order, 20 people.",
            daysAgo = 3, hour = 14, minute = 10,
        ),
        SeedTransaction(
            name = "Electric bill", category = "Utilities", amount = 142.30, type = TxnType.SPEND,
            method = "Bank transfer", paid = true, remarks = "", daysAgo = 3, hour = 8, minute = 47,
        ),
        SeedTransaction(
            name = "POS batch settlement", category = "Sales", amount = 465.75, type = TxnType.INCOME,
            method = "Card", paid = true, remarks = "", daysAgo = 4, hour = 17, minute = 58,
        ),
        SeedTransaction(
            name = "Payroll — part-time staff", category = "Wages", amount = 1180.00, type = TxnType.SPEND,
            method = "Bank transfer", paid = true, remarks = "Biweekly payroll, 3 staff.",
            daysAgo = 4, hour = 16, minute = 0,
        ),
        SeedTransaction(
            name = "POS batch settlement", category = "Sales", amount = 398.10, type = TxnType.INCOME,
            method = "Card", paid = true, remarks = "", daysAgo = 6, hour = 18, minute = 12,
        ),
        SeedTransaction(
            name = "Pastry supplier invoice", category = "Inventory & Supplies", amount = 210.65, type = TxnType.SPEND,
            method = "Invoice", paid = false, remarks = "Monthly pastry invoice.",
            daysAgo = 6, hour = 10, minute = 5,
        ),
        SeedTransaction(
            name = "Espresso machine service", category = "Equipment", amount = 60.00, type = TxnType.SPEND,
            method = "Cash", paid = true, remarks = "", daysAgo = 6, hour = 13, minute = 30,
        ),
    )

    /** No `id` — these are inserted fresh, Room assigns the real row id. */
    data class StockSeed(val name: String, val qty: Double, val unit: String, val low: Double)

    data class TodoSeed(val title: String, val desc: String, val done: Boolean)

    val stock: List<StockSeed> = listOf(
        StockSeed(name = "Espresso beans", qty = 14.0, unit = "kg", low = 5.0),
        StockSeed(name = "Oat milk", qty = 22.0, unit = "L", low = 10.0),
        StockSeed(name = "Whole milk", qty = 18.0, unit = "L", low = 10.0),
        StockSeed(name = "Paper cups (12oz)", qty = 340.0, unit = "pcs", low = 150.0),
        StockSeed(name = "Croissants", qty = 6.0, unit = "pcs", low = 8.0),
        StockSeed(name = "Sugar packets", qty = 900.0, unit = "pcs", low = 200.0),
    )

    val todos: List<TodoSeed> = listOf(
        TodoSeed(
            title = "Reconcile August bank statement",
            desc = "Match POS deposits against the bank feed before closing the month.",
            done = false,
        ),
        TodoSeed(
            title = "Renew food handler’s permit",
            desc = "City permit expires Sept 15 — submit renewal paperwork.",
            done = false,
        ),
        TodoSeed(
            title = "Follow up on Hyde & Co. invoice",
            desc = "Catering invoice from Aug 27 is still unpaid, $540.00.",
            done = false,
        ),
        TodoSeed(
            title = "Order espresso machine parts",
            desc = "Replacement gasket and group head seal from supplier.",
            done = true,
        ),
        TodoSeed(
            title = "Schedule deep clean for espresso bar",
            desc = "Book the quarterly descale and backflush service.",
            done = false,
        ),
        TodoSeed(
            title = "Review Instagram ad spend",
            desc = "Check performance on the weekend promo before renewing.",
            done = true,
        ),
    )

    val defaultCategories: List<String> = listOf(
        "Sales", "Catering & Events", "Other Income", "Inventory & Supplies",
        "Rent", "Utilities", "Wages", "Marketing", "Equipment",
    )
}
