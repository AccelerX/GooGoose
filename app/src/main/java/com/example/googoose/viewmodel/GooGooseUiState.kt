package com.example.googoose.viewmodel

import com.example.googoose.data.Language
import com.example.googoose.data.SeedData
import com.example.googoose.data.TextSizePreset
import com.example.googoose.data.model.StockItem
import com.example.googoose.data.model.TodoItem
import com.example.googoose.data.model.Transaction
import com.example.googoose.data.model.TxnType

enum class TxnFilter { ALL, INCOME, SPEND, UNSOLVED }

/** Reports-page date scope — rolling windows anchored to now, not calendar-aligned. */
enum class ReportRange { ALL, MONTH_30D, WEEK_7D, DAY_TODAY }

/** Pending +/- stock edit awaiting confirmation — mirrors `state.stockConfirm` in Till.dc.html. */
data class StockConfirm(
    val id: Long,
    val delta: Double,
    val isIncrease: Boolean,
    val name: String,
    val unit: String,
    val newQty: Double,
)

/**
 * Single source of truth for the whole app. `transactions`/`stock`/`todos`/
 * `categories`/`settingsName`/`settingsCurrency`/`language` are now backed by
 * Room via [GooGooseRepository] Flows (see [GooGooseViewModel]) — real local
 * persistence, not in-memory-only defaults.
 */
data class GooGooseUiState(
    val tab: Int = 0,
    val filter: TxnFilter = TxnFilter.ALL,
    /** Local midnight of the cutoff date (inclusive of that whole day) — null means no date filter. Combines with [filter]. */
    val dateBeforeFilter: Long? = null,

    val transactions: List<Transaction> = emptyList(),

    val stock: List<StockItem> = emptyList(),
    /** Keyed by stock row id (not list position) so removing a row can't misalign another row's amount box. */
    val stockAmounts: Map<Long, String> = emptyMap(),
    val stockConfirm: StockConfirm? = null,
    val showAddStock: Boolean = false,
    val newStockName: String = "",
    val newStockQty: String = "",
    val newStockUnit: String = "",
    /** Blank = auto-default to 20% of [newStockQty] (see GooGooseLogic.lowStockThreshold). */
    val newStockLow: String = "",

    val todos: List<TodoItem> = emptyList(),
    val showAddTodo: Boolean = false,
    val newTodoTitle: String = "",
    val newTodoDesc: String = "",

    val categories: List<String> = emptyList(),
    val addingCategory: Boolean = false,
    val newCategoryName: String = "",

    val showSettings: Boolean = false,
    val settingsName: String = SeedData.defaultBusinessName,
    val settingsCurrency: String = "USD",
    val language: Language = Language.EN,
    val textSize: TextSizePreset = TextSizePreset.STANDARD,
    val importMessage: String? = null,

    val showSheet: Boolean = false,
    val sheetType: TxnType = TxnType.SPEND,

    val reportRange: ReportRange = ReportRange.ALL,
    val reportCategoryType: TxnType = TxnType.SPEND,
    val excludedReportCategories: Set<String> = emptySet(),

    val detailItem: Transaction? = null,
    val detailCategory: String? = null,
    val detailMethod: String = "",
    val detailPaid: Boolean? = null,
)
