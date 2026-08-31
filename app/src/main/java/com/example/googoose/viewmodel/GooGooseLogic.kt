package com.example.googoose.viewmodel

import androidx.compose.ui.graphics.Color
import com.example.googoose.data.Language
import com.example.googoose.data.model.StockItem
import com.example.googoose.data.model.TodoItem
import com.example.googoose.data.model.Transaction
import com.example.googoose.data.model.TransactionGroup
import com.example.googoose.data.model.TxnType
import com.example.googoose.ui.theme.CategoryColors
import com.example.googoose.util.DateFormat
import com.example.googoose.util.formatMoney
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Pure, stateless derivations over real transaction data — the Kotlin
 * counterpart of the computed locals inside `renderVals()` in Till.dc.html,
 * now fed by Room instead of static sample tables. Side-effect free so it's
 * unit-testable without a ViewModel/Compose runtime ([Color] is a plain
 * value type here, not a Composable).
 */
object GooGooseLogic {

    private const val ONE_DAY_MILLIS = 24L * 60 * 60 * 1000

    private fun startOfDay(millis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    /**
     * [transactions] must already be sorted newest-first (the DAO query
     * guarantees this). [beforeDate], if set, is a local-midnight cutoff
     * (see [DateFormat.localDayStart]) — inclusive of that whole day — and
     * combines with [filter] rather than replacing it.
     */
    fun filteredGroups(
        filter: TxnFilter,
        transactions: List<Transaction>,
        now: Long = System.currentTimeMillis(),
        beforeDate: Long? = null,
        language: Language = Language.EN,
    ): List<TransactionGroup> {
        val cutoffExclusive = beforeDate?.let { it + ONE_DAY_MILLIS }
        val filtered = transactions.filter { item ->
            val matchesType = when (filter) {
                TxnFilter.ALL -> true
                TxnFilter.INCOME -> item.type == TxnType.INCOME
                TxnFilter.SPEND -> item.type == TxnType.SPEND
                TxnFilter.UNSOLVED -> !item.paid
            }
            val matchesDate = cutoffExclusive == null || item.occurredAt < cutoffExclusive
            matchesType && matchesDate
        }
        return filtered
            .groupBy { DateFormat.dateGroupLabel(it.occurredAt, now, language) }
            .map { (label, items) -> TransactionGroup(date = label, items = items) }
    }

    fun lowStockCount(stock: List<StockItem>): Int = stock.count { it.qty <= it.low }

    /** Auto low-threshold for a newly added stock item: 20% of its starting quantity. */
    fun lowStockThreshold(startingQty: Double): Double = ceil(startingQty * 0.2)

    /**
     * Undone items first, done items after — both groups in original
     * (insertion/id-ascending) order. `sortedBy` is a stable sort, and
     * [todos] already arrives id-ascending from the DAO, so this one line
     * reproduces the mockup's `origIndex`-based partition exactly.
     */
    fun sortedTodos(todos: List<TodoItem>): List<TodoItem> = todos.sortedBy { it.done }

    data class MonthSummary(val incomeLabel: String, val spendLabel: String, val netLabel: String)

    private fun isSameMonth(millis: Long, cal: Calendar): Boolean {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        return c.get(Calendar.YEAR) == cal.get(Calendar.YEAR) && c.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
    }

    private fun netLabel(net: Double, currencySymbol: String): String =
        (if (net >= 0) "+$currencySymbol" else "-$currencySymbol") + formatMoney(abs(net))

    /** This calendar month's income/spend/net — backs the Transactions-tab balance card's "this month" tag. Not used by Reports (see [rangeSummary]). */
    fun monthSummary(
        transactions: List<Transaction>,
        now: Long = System.currentTimeMillis(),
        currencySymbol: String = "$",
    ): MonthSummary {
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val inMonth = transactions.filter { isSameMonth(it.occurredAt, cal) }
        val income = inMonth.filter { it.type == TxnType.INCOME }.sumOf { it.amount }
        val spend = inMonth.filter { it.type == TxnType.SPEND }.sumOf { it.amount }
        return MonthSummary(
            incomeLabel = currencySymbol + formatMoney(income),
            spendLabel = currencySymbol + formatMoney(spend),
            netLabel = netLabel(income - spend, currencySymbol),
        )
    }

    /** All-time running balance (all income minus all spend) for the Transactions-tab balance card. */
    fun accountBalanceLabel(transactions: List<Transaction>, currencySymbol: String = "$"): String {
        val balance = transactions.sumOf { if (it.type == TxnType.INCOME) it.amount else -it.amount }
        return currencySymbol + formatMoney(balance)
    }

    /** Same "this month net" figure shown next to the balance card, reusing [monthSummary]. */
    fun monthNetLabel(transactions: List<Transaction>, now: Long = System.currentTimeMillis(), currencySymbol: String = "$"): String =
        monthSummary(transactions, now, currencySymbol).netLabel

    /** Rolling windows anchored to [now] — Reports' own date-range selector, independent of calendar months. */
    private fun withinRange(occurredAt: Long, now: Long, range: ReportRange): Boolean = when (range) {
        ReportRange.ALL -> occurredAt <= now
        ReportRange.MONTH_30D -> occurredAt in (now - 30L * ONE_DAY_MILLIS)..now
        ReportRange.WEEK_7D -> occurredAt in (now - 7L * ONE_DAY_MILLIS)..now
        ReportRange.DAY_TODAY -> occurredAt in startOfDay(now)..now
    }

    /** Reports stat cards — [range]-scoped income/spend/net (the "Last 7 days" chart below is intentionally NOT scoped by this — it's always the trailing 7 days). */
    fun rangeSummary(
        transactions: List<Transaction>,
        range: ReportRange,
        now: Long = System.currentTimeMillis(),
        currencySymbol: String = "$",
    ): MonthSummary {
        val inRange = transactions.filter { withinRange(it.occurredAt, now, range) }
        val income = inRange.filter { it.type == TxnType.INCOME }.sumOf { it.amount }
        val spend = inRange.filter { it.type == TxnType.SPEND }.sumOf { it.amount }
        return MonthSummary(
            incomeLabel = currencySymbol + formatMoney(income),
            spendLabel = currencySymbol + formatMoney(spend),
            netLabel = netLabel(income - spend, currencySymbol),
        )
    }

    data class CategoryBreakdownRow(
        val category: String,
        val amountLabel: String,
        /** Bar width relative to the top category (0–100). */
        val barPct: Int,
        /** Pie-slice share of the whole breakdown (0–1), already includes the "Other" row if present. */
        val pieFraction: Float,
        val color: Color,
    )

    /**
     * [type]/[range]-scoped spend-or-income grouped by category, sorted
     * descending, [excludedCategories] removed first. Colors are assigned by
     * sorted RANK (largest = slot 1), not by category identity — see
     * [CategoryColors] for why: it's what keeps every adjacency actually
     * on-screen (bar-list neighbors AND the pie's wrap-around) inside the
     * validated hue chain. Categories beyond [CategoryColors.maxSlots] fold
     * into a single [otherLabel] row rather than reusing a hue.
     */
    fun categoryBreakdown(
        transactions: List<Transaction>,
        type: TxnType,
        range: ReportRange,
        excludedCategories: Set<String>,
        now: Long = System.currentTimeMillis(),
        currencySymbol: String = "$",
        otherLabel: String = "Other",
    ): List<CategoryBreakdownRow> {
        val inScope = transactions.filter {
            it.type == type && withinRange(it.occurredAt, now, range) && it.category !in excludedCategories
        }
        val byCategory = inScope
            .groupBy { it.category }
            .map { (cat, items) -> cat to items.sumOf { it.amount } }
            .sortedByDescending { it.second }

        val head = byCategory.take(CategoryColors.maxSlots)
        val otherTotal = byCategory.drop(CategoryColors.maxSlots).sumOf { it.second }

        val entries = head.mapIndexed { rank, (cat, amount) -> Triple(cat, amount, CategoryColors.forRank(rank)) } +
            if (otherTotal > 0.0) listOf(Triple(otherLabel, otherTotal, CategoryColors.other)) else emptyList()

        val maxAmount = entries.maxOfOrNull { it.second } ?: 0.0
        val total = entries.sumOf { it.second }
        return entries.map { (cat, amount, color) ->
            CategoryBreakdownRow(
                category = cat,
                amountLabel = currencySymbol + formatMoney(amount),
                barPct = if (maxAmount <= 0.0) 0 else min(100, (amount / maxAmount * 100).roundToInt()),
                pieFraction = if (total <= 0.0) 0f else (amount / total).toFloat(),
                color = color,
            )
        }
    }

    data class WeekBar(
        val label: String,
        /** Unambiguous "Aug 28" — [label] repeats letters (two T's, two S's), so the low-cash-flow warning names days with this instead. */
        val shortDate: String,
        val netLabel: String,
        val heightDp: Int,
        val isNegative: Boolean,
        /** Negative, or net magnitude under 15% of the week's average daily turnover — see the low-cash-flow warning below the chart. */
        val isWarning: Boolean,
    )

    private const val NEAR_ZERO_FRACTION = 0.15

    /** Always the trailing 7 days regardless of the Reports date-range selector (see [rangeSummary]'s doc). Bar height in dp, capped at 44, floor 4. */
    fun weekBars(
        transactions: List<Transaction>,
        now: Long = System.currentTimeMillis(),
        currencySymbol: String = "$",
        language: Language = Language.EN,
    ): List<WeekBar> {
        val todayStart = startOfDay(now)

        val days = (6 downTo 0).map { offset ->
            val dayStart = todayStart - offset * ONE_DAY_MILLIS
            val dayEnd = dayStart + ONE_DAY_MILLIS
            val dayTxns = transactions.filter { it.occurredAt in dayStart until dayEnd }
            val net = dayTxns.sumOf { if (it.type == TxnType.INCOME) it.amount else -it.amount }
            val flow = dayTxns.sumOf { it.amount }
            Triple(dayStart, net, flow)
        }

        val avgFlow = days.map { it.third }.average().let { if (it.isNaN()) 0.0 else it }
        val warnThreshold = avgFlow * NEAR_ZERO_FRACTION
        val maxAbsNet = days.maxOfOrNull { abs(it.second) } ?: 0.0

        return days.map { (dayStart, net, _) ->
            WeekBar(
                label = DateFormat.weekdayInitial(dayStart, language),
                shortDate = DateFormat.shortDate(dayStart, language),
                netLabel = (if (net >= 0) "+" else "-") + currencySymbol + abs(net).roundToInt(),
                heightDp = if (maxAbsNet <= 0.0) 4 else max(4, (abs(net) / maxAbsNet * 44).roundToInt()),
                isNegative = net < 0,
                isWarning = net < 0 || abs(net) < warnThreshold,
            )
        }
    }
}
