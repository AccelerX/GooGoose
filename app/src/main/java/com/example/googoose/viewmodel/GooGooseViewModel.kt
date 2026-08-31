package com.example.googoose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googoose.data.GooGooseRepository
import com.example.googoose.data.Language
import com.example.googoose.data.StockImport
import com.example.googoose.data.TextSizePreset
import com.example.googoose.data.TodoImport
import com.example.googoose.data.TransactionImport
import com.example.googoose.data.model.Transaction
import com.example.googoose.data.model.TxnType
import com.example.googoose.data.stringsFor
import kotlin.math.max
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/** Ephemeral navigation/form state that is never written to disk. */
private data class UiOnlyState(
    val tab: Int = 0,
    val filter: TxnFilter = TxnFilter.ALL,
    val dateBeforeFilter: Long? = null,
    val stockAmounts: Map<Long, String> = emptyMap(),
    val stockConfirm: StockConfirm? = null,
    val showAddStock: Boolean = false,
    val newStockName: String = "",
    val newStockQty: String = "",
    val newStockUnit: String = "",
    val newStockLow: String = "",
    val showAddTodo: Boolean = false,
    val newTodoTitle: String = "",
    val newTodoDesc: String = "",
    val addingCategory: Boolean = false,
    val newCategoryName: String = "",
    val showSettings: Boolean = false,
    val importMessage: String? = null,
    val showSheet: Boolean = false,
    val sheetType: TxnType = TxnType.SPEND,
    val reportRange: ReportRange = ReportRange.ALL,
    val reportCategoryType: TxnType = TxnType.SPEND,
    val excludedReportCategories: Set<String> = emptySet(),
    val detailItemId: Long? = null,
    val detailCategory: String? = null,
    val detailMethod: String = "",
    val detailPaid: Boolean? = null,
)

private data class PersistedSnapshot(
    val transactions: List<Transaction>,
    val stock: List<com.example.googoose.data.model.StockItem>,
    val todos: List<com.example.googoose.data.model.TodoItem>,
    val categories: List<String>,
    val settingsName: String,
    val settingsCurrency: String,
    val language: Language,
    val textSize: TextSizePreset,
)

/**
 * Holds the single [GooGooseUiState], sourced from real Room-backed data via
 * [repository] merged with local-only navigation/form state. Every data
 * mutation is a suspend call into the repository now — nothing here mutates
 * state in place the way the mockup-fidelity v1 did.
 */
class GooGooseViewModel(private val repository: GooGooseRepository) : ViewModel() {

    private val _uiOnly = MutableStateFlow(UiOnlyState())

    private val persisted = combine(
        repository.transactions,
        repository.stock,
        repository.todos,
        repository.categories,
        repository.settings,
    ) { transactions, stock, todos, categories, settings ->
        PersistedSnapshot(
            transactions = transactions,
            stock = stock,
            todos = todos,
            categories = categories,
            settingsName = settings.businessName,
            settingsCurrency = settings.currency,
            language = settings.language,
            textSize = settings.textSize,
        )
    }

    val state: StateFlow<GooGooseUiState> = combine(persisted, _uiOnly) { p, ui ->
        GooGooseUiState(
            tab = ui.tab,
            filter = ui.filter,
            dateBeforeFilter = ui.dateBeforeFilter,
            transactions = p.transactions,
            stock = p.stock,
            stockAmounts = ui.stockAmounts,
            stockConfirm = ui.stockConfirm,
            showAddStock = ui.showAddStock,
            newStockName = ui.newStockName,
            newStockQty = ui.newStockQty,
            newStockUnit = ui.newStockUnit,
            newStockLow = ui.newStockLow,
            todos = p.todos,
            showAddTodo = ui.showAddTodo,
            newTodoTitle = ui.newTodoTitle,
            newTodoDesc = ui.newTodoDesc,
            categories = p.categories,
            addingCategory = ui.addingCategory,
            newCategoryName = ui.newCategoryName,
            showSettings = ui.showSettings,
            settingsName = p.settingsName,
            settingsCurrency = p.settingsCurrency,
            language = p.language,
            textSize = p.textSize,
            importMessage = ui.importMessage,
            showSheet = ui.showSheet,
            sheetType = ui.sheetType,
            reportRange = ui.reportRange,
            reportCategoryType = ui.reportCategoryType,
            excludedReportCategories = ui.excludedReportCategories,
            detailItem = ui.detailItemId?.let { id -> p.transactions.find { it.id == id } },
            detailCategory = ui.detailCategory,
            detailMethod = ui.detailMethod,
            detailPaid = ui.detailPaid,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GooGooseUiState())

    private fun updateUi(transform: (UiOnlyState) -> UiOnlyState) {
        _uiOnly.value = transform(_uiOnly.value)
    }

    // ---- Tabs & transaction filter ----

    fun goTab(index: Int) = updateUi { it.copy(tab = index) }

    fun setFilter(filter: TxnFilter) = updateUi { it.copy(filter = filter) }

    /** Pass null to clear the "before date" filter. */
    fun setDateBeforeFilter(localDayStart: Long?) = updateUi { it.copy(dateBeforeFilter = localDayStart) }

    // ---- Stock tab ----

    fun setStockAmount(id: Long, value: String) = updateUi { it.copy(stockAmounts = it.stockAmounts + (id to value)) }

    fun setStockUnit(id: Long, unit: String) {
        viewModelScope.launch { repository.setStockUnit(id, unit) }
    }

    fun removeStock(id: Long) {
        viewModelScope.launch { repository.removeStock(id) }
        updateUi { it.copy(stockAmounts = it.stockAmounts - id) }
    }

    fun requestIncrease(id: Long) {
        val s = state.value
        val item = s.stock.find { it.id == id } ?: return
        val delta = resolveStockDelta(s.stockAmounts[id] ?: "1")
        updateUi { it.copy(stockConfirm = StockConfirm(id, delta, true, item.name, item.unit, item.qty + delta)) }
    }

    fun requestDecrease(id: Long) {
        val s = state.value
        val item = s.stock.find { it.id == id } ?: return
        val delta = resolveStockDelta(s.stockAmounts[id] ?: "1")
        val newQty = max(0.0, item.qty - delta)
        updateUi { it.copy(stockConfirm = StockConfirm(id, -delta, false, item.name, item.unit, newQty)) }
    }

    fun cancelStockChange() = updateUi { it.copy(stockConfirm = null) }

    fun confirmStockChange() {
        val confirm = _uiOnly.value.stockConfirm ?: return
        viewModelScope.launch { repository.setStockQty(confirm.id, confirm.newQty) }
        updateUi { it.copy(stockConfirm = null) }
    }

    /** `parseFloat(amount) || 1` then floored at 0 — ported as-is, quirks included. */
    private fun resolveStockDelta(amountText: String): Double {
        val parsed = amountText.toDoubleOrNull()
        val base = if (parsed == null || parsed == 0.0) 1.0 else parsed
        return max(0.0, base)
    }

    fun openAddStock() = updateUi {
        it.copy(showAddStock = true, newStockName = "", newStockQty = "", newStockUnit = "", newStockLow = "")
    }

    fun closeAddStock() = updateUi { it.copy(showAddStock = false) }

    fun setNewStockName(value: String) = updateUi { it.copy(newStockName = value) }

    fun setNewStockQty(value: String) = updateUi { it.copy(newStockQty = value) }

    fun setNewStockUnit(value: String) = updateUi { it.copy(newStockUnit = value) }

    fun setNewStockLow(value: String) = updateUi { it.copy(newStockLow = value) }

    fun saveNewStock() {
        val s = _uiOnly.value
        if (s.newStockName.isBlank()) return
        val qty = s.newStockQty.toDoubleOrNull() ?: 0.0
        val unit = s.newStockUnit.ifBlank { "pcs" }
        val low = s.newStockLow.toDoubleOrNull() ?: GooGooseLogic.lowStockThreshold(qty)
        viewModelScope.launch { repository.addStock(s.newStockName, qty, unit, low) }
        updateUi { it.copy(showAddStock = false) }
    }

    // ---- Todo tab ----

    fun toggleTodo(id: Long) {
        viewModelScope.launch { repository.toggleTodo(id) }
    }

    /** Only completed tasks show a delete affordance in the UI; deletion itself is unconditional here. */
    fun deleteTodo(id: Long) {
        viewModelScope.launch { repository.deleteTodo(id) }
    }

    fun openAddTodo() = updateUi { it.copy(showAddTodo = true, newTodoTitle = "", newTodoDesc = "") }

    fun closeAddTodo() = updateUi { it.copy(showAddTodo = false) }

    fun setNewTodoTitle(value: String) = updateUi { it.copy(newTodoTitle = value) }

    fun setNewTodoDesc(value: String) = updateUi { it.copy(newTodoDesc = value) }

    fun saveNewTodo() {
        val s = _uiOnly.value
        if (s.newTodoTitle.isBlank()) return
        viewModelScope.launch { repository.addTodo(s.newTodoTitle, s.newTodoDesc) }
        updateUi { it.copy(showAddTodo = false) }
    }

    // ---- Category chips (shared by Edit transaction) ----

    fun selectCategory(name: String) = updateUi { it.copy(detailCategory = name) }

    fun removeCategory(name: String) {
        viewModelScope.launch { repository.removeCategory(name) }
        updateUi { it.copy(detailCategory = if (it.detailCategory == name) null else it.detailCategory) }
    }

    fun startAddCategory() = updateUi { it.copy(addingCategory = true) }

    fun setNewCategoryName(value: String) = updateUi { it.copy(newCategoryName = value) }

    fun confirmAddCategory() {
        val name = _uiOnly.value.newCategoryName.trim()
        if (name.isEmpty()) {
            updateUi { it.copy(addingCategory = false) }
            return
        }
        viewModelScope.launch { repository.addCategory(name) }
        updateUi { it.copy(detailCategory = name, addingCategory = false, newCategoryName = "") }
    }

    // ---- Edit transaction overlay ----

    fun openDetail(transaction: Transaction) {
        updateUi {
            it.copy(
                detailItemId = transaction.id,
                detailCategory = transaction.category,
                detailMethod = transaction.method,
                detailPaid = transaction.paid,
            )
        }
    }

    fun closeDetail() = updateUi { it.copy(detailItemId = null, addingCategory = false, newCategoryName = "") }

    /** Deletes the transaction currently open in the detail screen, then closes it — the delete itself is unrecoverable, so the screen gates this behind its own confirmation dialog. */
    fun deleteTransaction() {
        val id = _uiOnly.value.detailItemId ?: return
        viewModelScope.launch { repository.deleteTransaction(id) }
        updateUi { it.copy(detailItemId = null, addingCategory = false, newCategoryName = "") }
    }

    fun setDetailMethod(value: String) = updateUi { it.copy(detailMethod = value) }

    fun setDetailPaid(paid: Boolean) = updateUi { it.copy(detailPaid = paid) }

    /** Amount/description/remarks come from the screen's own controlled fields at Save time. */
    fun saveDetail(amount: Double, name: String, remarks: String) {
        val ui = _uiOnly.value
        val id = ui.detailItemId ?: return
        val category = ui.detailCategory ?: return
        viewModelScope.launch {
            repository.updateTransaction(id, name, category, amount, ui.detailMethod, ui.detailPaid ?: true, remarks)
        }
        updateUi { it.copy(detailItemId = null, addingCategory = false, newCategoryName = "") }
    }

    // ---- Add transaction sheet ----

    fun openSheet() = updateUi { it.copy(showSheet = true) }

    fun closeSheet() = updateUi { it.copy(showSheet = false) }

    fun setSheetType(type: TxnType) = updateUi { it.copy(sheetType = type) }

    // ---- Reports tab: date range, spend/income-by-category, category picker ----

    fun setReportRange(range: ReportRange) = updateUi { it.copy(reportRange = range) }

    fun setReportCategoryType(type: TxnType) = updateUi { it.copy(reportCategoryType = type) }

    fun toggleReportCategory(name: String) = updateUi {
        it.copy(
            excludedReportCategories = if (name in it.excludedReportCategories) {
                it.excludedReportCategories - name
            } else {
                it.excludedReportCategories + name
            },
        )
    }

    /** New transactions have no method/paid-status field in the Add sheet — repository applies defaults. */
    fun addTransaction(name: String, category: String, amount: Double, occurredAt: Long) {
        val type = _uiOnly.value.sheetType
        viewModelScope.launch { repository.addTransaction(name, category, amount, type, occurredAt) }
        updateUi { it.copy(showSheet = false) }
    }

    // ---- Settings ----

    fun openSettings() = updateUi { it.copy(showSettings = true) }

    fun closeSettings() = updateUi { it.copy(showSettings = false) }

    fun setSettingsName(value: String) {
        viewModelScope.launch { repository.setBusinessName(value) }
    }

    fun setSettingsCurrency(value: String) {
        viewModelScope.launch { repository.setCurrency(value) }
    }

    fun setSettingsLanguage(language: Language) {
        viewModelScope.launch { repository.setLanguage(language) }
    }

    fun setTextSize(preset: TextSizePreset) {
        viewModelScope.launch { repository.setTextSize(preset) }
    }

    /** Full snapshot — transactions/stock/todos/categories/businessName/currency/language. */
    suspend fun buildExportJson(): String {
        val txns = repository.transactions.first()
        val stock = repository.stock.first()
        val todos = repository.todos.first()
        val categories = repository.categories.first()
        val settings = repository.settings.first()

        val root = JSONObject()
        root.put("businessName", settings.businessName)
        root.put("currency", settings.currency)
        root.put("language", settings.language.name)
        root.put("textSize", settings.textSize.name)
        root.put("categories", JSONArray(categories))
        root.put(
            "stock",
            JSONArray().apply {
                stock.forEach { item ->
                    put(
                        JSONObject()
                            .put("name", item.name)
                            .put("qty", item.qty)
                            .put("unit", item.unit)
                            .put("low", item.low),
                    )
                }
            },
        )
        root.put(
            "todos",
            JSONArray().apply {
                todos.forEach { todo ->
                    put(JSONObject().put("title", todo.title).put("desc", todo.desc).put("done", todo.done))
                }
            },
        )
        root.put(
            "transactions",
            JSONArray().apply {
                txns.forEach { t ->
                    put(
                        JSONObject()
                            .put("name", t.name)
                            .put("category", t.category)
                            .put("amount", t.amount)
                            .put("type", t.type.name)
                            .put("method", t.method)
                            .put("paid", t.paid)
                            .put("remarks", t.remarks)
                            .put("occurredAt", t.occurredAt)
                            .put("createdAt", t.createdAt)
                            .put("modifiedAt", t.modifiedAt),
                    )
                }
            },
        )
        return root.toString(2)
    }

    /** Full replace — clears and reinserts every table from the parsed JSON in one transaction. */
    fun importData(fileName: String, jsonText: String) {
        viewModelScope.launch {
            val strings = stringsFor(state.value.language)
            try {
                val root = JSONObject(jsonText)
                val categories = root.getJSONArray("categories").let { arr -> (0 until arr.length()).map { arr.getString(it) } }
                val stock = root.getJSONArray("stock").let { arr ->
                    (0 until arr.length()).map { i ->
                        val o = arr.getJSONObject(i)
                        StockImport(
                            name = o.getString("name"),
                            qty = o.getDouble("qty"),
                            unit = o.getString("unit"),
                            low = o.getDouble("low"),
                        )
                    }
                }
                val todos = root.getJSONArray("todos").let { arr ->
                    (0 until arr.length()).map { i ->
                        val o = arr.getJSONObject(i)
                        TodoImport(title = o.getString("title"), desc = o.getString("desc"), done = o.getBoolean("done"))
                    }
                }
                val transactions = root.getJSONArray("transactions").let { arr ->
                    (0 until arr.length()).map { i ->
                        val o = arr.getJSONObject(i)
                        TransactionImport(
                            name = o.getString("name"),
                            category = o.getString("category"),
                            amount = o.getDouble("amount"),
                            type = TxnType.valueOf(o.getString("type")),
                            method = o.getString("method"),
                            paid = o.getBoolean("paid"),
                            remarks = o.getString("remarks"),
                            occurredAt = o.getLong("occurredAt"),
                            createdAt = o.getLong("createdAt"),
                            modifiedAt = o.getLong("modifiedAt"),
                        )
                    }
                }
                repository.restoreAll(
                    businessName = root.getString("businessName"),
                    currency = root.getString("currency"),
                    language = Language.valueOf(root.optString("language", "EN")),
                    textSize = TextSizePreset.valueOf(root.optString("textSize", "STANDARD")),
                    categories = categories,
                    stock = stock,
                    todos = todos,
                    transactions = transactions,
                )
                updateUi { it.copy(importMessage = strings.importedMsg(fileName)) }
            } catch (e: Exception) {
                updateUi { it.copy(importMessage = strings.importError) }
            }
        }
    }
}
