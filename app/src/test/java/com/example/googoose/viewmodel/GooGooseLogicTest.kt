package com.example.googoose.viewmodel

import com.example.googoose.data.model.StockItem
import com.example.googoose.data.model.TodoItem
import com.example.googoose.data.model.Transaction
import com.example.googoose.data.model.TxnType
import com.example.googoose.util.DateFormat
import org.junit.Assert.assertEquals
import org.junit.Test

class GooGooseLogicTest {

    private fun txn(
        id: Long,
        category: String = "Sales",
        amount: Double = 10.0,
        type: TxnType = TxnType.SPEND,
        occurredAt: Long,
    ) = Transaction(
        id = id, name = "x", category = category, amount = amount, type = type,
        method = "", paid = true, remarks = "",
        occurredAt = occurredAt, createdAt = occurredAt, modifiedAt = occurredAt,
    )

    // ---- sortedTodos ----

    @Test
    fun `undone todos come first, done todos appended, both in original order`() {
        val todos = listOf(
            TodoItem(id = 1, title = "a", desc = "", done = false),
            TodoItem(id = 2, title = "b", desc = "", done = true),
            TodoItem(id = 3, title = "c", desc = "", done = false),
            TodoItem(id = 4, title = "d", desc = "", done = true),
            TodoItem(id = 5, title = "e", desc = "", done = false),
        )
        val sorted = GooGooseLogic.sortedTodos(todos)
        assertEquals(listOf(1L, 3L, 5L, 2L, 4L), sorted.map { it.id })
    }

    // ---- lowStockThreshold / lowStockCount ----

    @Test
    fun `low stock threshold is 20 percent of starting quantity, rounded up`() {
        assertEquals(3.0, GooGooseLogic.lowStockThreshold(14.0), 0.0)
        assertEquals(1.0, GooGooseLogic.lowStockThreshold(1.0), 0.0)
        assertEquals(0.0, GooGooseLogic.lowStockThreshold(0.0), 0.0)
    }

    @Test
    fun `low stock count only counts items at or below their own threshold`() {
        val stock = listOf(
            StockItem(id = 1, name = "a", qty = 5.0, unit = "kg", low = 5.0),
            StockItem(id = 2, name = "b", qty = 6.0, unit = "kg", low = 5.0),
            StockItem(id = 3, name = "c", qty = 4.0, unit = "kg", low = 5.0),
        )
        assertEquals(2, GooGooseLogic.lowStockCount(stock))
    }

    // ---- filteredGroups ----

    @Test
    fun `filter keeps only the matching type and drops empty date groups`() {
        val now = DateFormat.at(2026, 8, 30, 12, 0)
        val transactions = listOf(
            txn(id = 1, type = TxnType.INCOME, occurredAt = now),
            txn(id = 2, type = TxnType.SPEND, occurredAt = now),
            txn(id = 3, type = TxnType.SPEND, occurredAt = DateFormat.at(2026, 8, 29, 12, 0)),
        )
        val incomeOnly = GooGooseLogic.filteredGroups(TxnFilter.INCOME, transactions, now)
        assertEquals(listOf(1L), incomeOnly.flatMap { it.items }.map { it.id })
    }

    @Test
    fun `date groups label today and yesterday correctly`() {
        val now = DateFormat.at(2026, 8, 30, 12, 0)
        val transactions = listOf(
            txn(id = 1, occurredAt = DateFormat.at(2026, 8, 30, 9, 0)),
            txn(id = 2, occurredAt = DateFormat.at(2026, 8, 29, 9, 0)),
        )
        val groups = GooGooseLogic.filteredGroups(TxnFilter.ALL, transactions, now)
        val labels = groups.associate { it.date to it.items.map { t -> t.id } }
        assertEquals(listOf(1L), labels["Today"])
        assertEquals(listOf(2L), labels["Yesterday"])
    }

    // ---- monthSummary / accountBalanceLabel ----

    @Test
    fun `month summary sums only the current calendar month`() {
        val now = DateFormat.at(2026, 8, 30, 12, 0)
        val transactions = listOf(
            txn(id = 1, amount = 100.0, type = TxnType.INCOME, occurredAt = now),
            txn(id = 2, amount = 40.0, type = TxnType.SPEND, occurredAt = now),
            // last month — must not count
            txn(id = 3, amount = 999.0, type = TxnType.INCOME, occurredAt = DateFormat.at(2026, 7, 31, 12, 0)),
        )
        val summary = GooGooseLogic.monthSummary(transactions, now)
        assertEquals("$100.00", summary.incomeLabel)
        assertEquals("$40.00", summary.spendLabel)
        assertEquals("+$60.00", summary.netLabel)
    }

    @Test
    fun `account balance is all-time income minus spend, thousands-grouped`() {
        val transactions = listOf(
            txn(id = 1, amount = 6000.0, type = TxnType.INCOME, occurredAt = 0L),
            txn(id = 2, amount = 942.10, type = TxnType.SPEND, occurredAt = 0L),
        )
        assertEquals("$5,057.90", GooGooseLogic.accountBalanceLabel(transactions))
    }

    // ---- categoryBreakdown ----

    @Test
    fun `category breakdown sorts descending, scales bars, and excludes the other type`() {
        val now = DateFormat.at(2026, 8, 30, 12, 0)
        val transactions = listOf(
            txn(id = 1, category = "Rent", amount = 100.0, type = TxnType.SPEND, occurredAt = now),
            txn(id = 2, category = "Wages", amount = 200.0, type = TxnType.SPEND, occurredAt = now),
            txn(id = 3, category = "Wages", amount = 50.0, type = TxnType.SPEND, occurredAt = now),
            // income must be excluded from a SPEND breakdown
            txn(id = 4, category = "Sales", amount = 500.0, type = TxnType.INCOME, occurredAt = now),
        )
        val rows = GooGooseLogic.categoryBreakdown(
            transactions, TxnType.SPEND, ReportRange.ALL, excludedCategories = emptySet(), now = now,
        )
        assertEquals(listOf("Wages", "Rent"), rows.map { it.category })
        assertEquals(100, rows[0].barPct)
        assertEquals(40, rows[1].barPct)
    }

    @Test
    fun `category breakdown honors excludedCategories and folds overflow into Other`() {
        val now = DateFormat.at(2026, 8, 30, 12, 0)
        // 10 categories; excluding one leaves 9 — one more than CategoryColors.maxSlots (8) — so the smallest must fold into Other.
        val transactions = (1..10).map { i ->
            txn(id = i.toLong(), category = "Cat$i", amount = (11 - i).toDouble(), type = TxnType.SPEND, occurredAt = now)
        }
        val rows = GooGooseLogic.categoryBreakdown(
            transactions, TxnType.SPEND, ReportRange.ALL, excludedCategories = setOf("Cat1"), now = now, otherLabel = "Other",
        )
        // Cat1 (amount 10, the largest) is excluded; Cat2..Cat9 keep their own rows; Cat10 (amount 1, the smallest of the rest) folds into Other.
        assertEquals(
            listOf("Cat2", "Cat3", "Cat4", "Cat5", "Cat6", "Cat7", "Cat8", "Cat9", "Other"),
            rows.map { it.category },
        )
        assertEquals("$1.00", rows.last().amountLabel)
    }

    // ---- rangeSummary ----

    @Test
    fun `range summary only counts transactions inside the selected rolling window`() {
        val now = DateFormat.at(2026, 8, 30, 12, 0)
        val transactions = listOf(
            txn(id = 1, amount = 50.0, type = TxnType.INCOME, occurredAt = now), // today
            txn(id = 2, amount = 999.0, type = TxnType.INCOME, occurredAt = now - 20L * 24 * 60 * 60 * 1000), // 20 days ago
        )
        val week = GooGooseLogic.rangeSummary(transactions, ReportRange.WEEK_7D, now)
        val all = GooGooseLogic.rangeSummary(transactions, ReportRange.ALL, now)
        assertEquals("$50.00", week.incomeLabel)
        assertEquals("$1,049.00", all.incomeLabel)
    }

    // ---- weekBars warning ----

    @Test
    fun `weekBars flags negative and near-zero days relative to the week's average flow`() {
        val now = DateFormat.at(2026, 8, 30, 12, 0)
        val transactions = (0..6).flatMap { offset ->
            val day = now - offset.toLong() * 24 * 60 * 60 * 1000
            // Every day has $1000 of flow (income) except "today" (offset 0), which nets to just $5 — well under 15% of the ~$857 average.
            if (offset == 0) {
                listOf(txn(id = 100L, amount = 5.0, type = TxnType.INCOME, occurredAt = day))
            } else {
                listOf(txn(id = offset.toLong(), amount = 1000.0, type = TxnType.INCOME, occurredAt = day))
            }
        }
        val bars = GooGooseLogic.weekBars(transactions, now)
        assertEquals(true, bars.last().isWarning) // "today" is the last bar (6 downTo 0 order)
        assertEquals(false, bars.first().isWarning)
    }
}
