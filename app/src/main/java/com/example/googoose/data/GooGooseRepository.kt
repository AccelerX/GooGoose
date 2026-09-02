package com.example.googoose.data

import androidx.room.withTransaction
import com.example.googoose.data.db.CategoryEntity
import com.example.googoose.data.db.GooGooseDatabase
import com.example.googoose.data.db.PaymentMethodEntity
import com.example.googoose.data.db.SettingsEntity
import com.example.googoose.data.db.StockEntity
import com.example.googoose.data.db.TodoEntity
import com.example.googoose.data.db.TransactionEntity
import com.example.googoose.data.model.StockItem
import com.example.googoose.data.model.TodoItem
import com.example.googoose.data.model.Transaction
import com.example.googoose.data.model.TxnType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Plain, JSON-friendly shapes for a full-data Import — the ViewModel parses org.json into these. */
data class TransactionImport(
    val name: String,
    val category: String,
    val amount: Double,
    val type: TxnType,
    val method: String,
    val paid: Boolean,
    val remarks: String,
    val occurredAt: Long,
    val createdAt: Long,
    val modifiedAt: Long,
)

data class StockImport(val name: String, val qty: Double, val unit: String, val low: Double)
data class TodoImport(val title: String, val desc: String, val done: Boolean)

/**
 * Single data-access surface over [GooGooseDatabase] — the ViewModel observes
 * the Flow properties and calls the suspend functions for every mutation.
 * Nothing here touches Compose or org.json; JSON marshalling stays in the
 * ViewModel, this only deals in domain/import shapes.
 */
class GooGooseRepository(private val db: GooGooseDatabase) {

    val transactions: Flow<List<Transaction>> = db.transactionDao().observeAll().map { list -> list.map { it.toDomain() } }
    val stock: Flow<List<StockItem>> = db.stockDao().observeAll().map { list -> list.map { it.toDomain() } }
    val todos: Flow<List<TodoItem>> = db.todoDao().observeAll().map { list -> list.map { it.toDomain() } }
    val categories: Flow<List<String>> = db.categoryDao().observeAll().map { list -> list.map { it.name } }
    val paymentMethods: Flow<List<String>> = db.paymentMethodDao().observeAll().map { list -> list.map { it.name } }
    val settings: Flow<SettingsEntity> = db.settingsDao().observe().map { it ?: defaultSettings() }

    private fun defaultSettings() = SettingsEntity(
        businessName = SeedData.defaultBusinessName,
        currency = "USD",
        language = Language.EN,
        textSize = TextSizePreset.STANDARD,
        hasOnboarded = false,
    )

    /**
     * Finishes the onboarding screen (business name/language/currency +
     * Sample-data-vs-empty choice — the Import choice goes through
     * [restoreAll] instead, via the same ViewModel.importData path Settings
     * already uses). [businessName] is caller-supplied: OnboardingScreen
     * passes [SeedData.defaultBusinessName] for the Sample choice and
     * whatever the user typed for the Empty choice. Default categories
     * always seed (they're a starter vocabulary, not "fake data"); demo
     * transactions/stock/todos/payment-method-history only seed when
     * [seedSampleData] is true.
     */
    suspend fun completeOnboarding(businessName: String, currency: String, language: Language, seedSampleData: Boolean) {
        db.withTransaction {
            db.settingsDao().upsert(
                SettingsEntity(
                    businessName = businessName,
                    currency = currency,
                    language = language,
                    hasOnboarded = true,
                ),
            )
            db.categoryDao().insertAll(SeedData.defaultCategories.map { CategoryEntity(name = it) })
            if (seedSampleData) {
                db.paymentMethodDao().insertAll(
                    SeedData.transactions.map { it.method }.distinct().map { PaymentMethodEntity(name = it) },
                )
                db.stockDao().insertAll(
                    SeedData.stock.map { s -> StockEntity(name = s.name, qty = s.qty, unit = s.unit, low = s.low) },
                )
                db.todoDao().insertAll(
                    SeedData.todos.map { t -> TodoEntity(title = t.title, desc = t.desc, done = t.done) },
                )
                db.transactionDao().insertAll(
                    SeedData.transactions.map { s ->
                        val occurredAt = s.occurredAt()
                        TransactionEntity(
                            name = s.name, category = s.category, amount = s.amount, type = s.type,
                            method = s.method, paid = s.paid, remarks = s.remarks,
                            occurredAt = occurredAt, createdAt = occurredAt, modifiedAt = occurredAt,
                        )
                    },
                )
            }
        }
    }

    // ---- transactions ----

    suspend fun addTransaction(
        name: String,
        category: String,
        amount: Double,
        type: TxnType,
        occurredAt: Long,
        method: String,
        paid: Boolean,
    ) {
        val now = System.currentTimeMillis()
        db.transactionDao().insert(
            TransactionEntity(
                name = name, category = category, amount = amount, type = type,
                method = method, paid = paid, remarks = "",
                occurredAt = occurredAt, createdAt = now, modifiedAt = now,
            ),
        )
        addPaymentMethodToHistory(method)
    }

    suspend fun deleteTransaction(id: Long) {
        db.transactionDao().deleteById(id)
    }

    suspend fun updateTransaction(
        id: Long,
        name: String,
        category: String,
        amount: Double,
        method: String,
        paid: Boolean,
        remarks: String,
    ) {
        val current = db.transactionDao().findById(id) ?: return
        db.transactionDao().update(
            current.copy(
                name = name, category = category, amount = amount,
                method = method, paid = paid, remarks = remarks,
                modifiedAt = System.currentTimeMillis(),
            ),
        )
    }

    // ---- stock ----

    suspend fun addStock(name: String, qty: Double, unit: String, low: Double) {
        db.stockDao().insert(StockEntity(name = name, qty = qty, unit = unit, low = low))
    }

    suspend fun setStockQty(id: Long, qty: Double) {
        val current = db.stockDao().findById(id) ?: return
        db.stockDao().update(current.copy(qty = qty))
    }

    /** Detail-page Save — name/quantity/unit/threshold together in one write, unlike the card's delta-based [setStockQty]. */
    suspend fun updateStockDetails(id: Long, name: String, qty: Double, unit: String, low: Double) {
        val current = db.stockDao().findById(id) ?: return
        db.stockDao().update(current.copy(name = name, qty = qty, unit = unit, low = low))
    }

    suspend fun removeStock(id: Long) {
        db.stockDao().deleteById(id)
    }

    // ---- todos ----

    suspend fun toggleTodo(id: Long) {
        val current = db.todoDao().findById(id) ?: return
        db.todoDao().update(current.copy(done = !current.done))
    }

    suspend fun addTodo(title: String, desc: String) {
        db.todoDao().insert(TodoEntity(title = title, desc = desc, done = false))
    }

    /** Detail-page Save — title/description together, `done` untouched. */
    suspend fun updateTodo(id: Long, title: String, desc: String) {
        val current = db.todoDao().findById(id) ?: return
        db.todoDao().update(current.copy(title = title, desc = desc))
    }

    suspend fun deleteTodo(id: Long) {
        db.todoDao().deleteById(id)
    }

    // ---- categories ----

    suspend fun addCategory(name: String) {
        if (!db.categoryDao().exists(name)) {
            db.categoryDao().insert(CategoryEntity(name = name))
        }
    }

    suspend fun removeCategory(name: String) {
        db.categoryDao().deleteByName(name)
    }

    // ---- payment method history ----

    suspend fun addPaymentMethodToHistory(name: String) {
        if (name.isNotBlank() && !db.paymentMethodDao().exists(name)) {
            db.paymentMethodDao().insert(PaymentMethodEntity(name = name))
        }
    }

    suspend fun removePaymentMethodFromHistory(name: String) {
        db.paymentMethodDao().deleteByName(name)
    }

    // ---- settings ----

    suspend fun setBusinessName(value: String) = updateSettings { it.copy(businessName = value) }
    suspend fun setCurrency(value: String) = updateSettings { it.copy(currency = value) }
    suspend fun setLanguage(value: Language) = updateSettings { it.copy(language = value) }
    suspend fun setTextSize(value: TextSizePreset) = updateSettings { it.copy(textSize = value) }

    private suspend fun updateSettings(transform: (SettingsEntity) -> SettingsEntity) {
        val current = db.settingsDao().get() ?: defaultSettings()
        db.settingsDao().upsert(transform(current))
    }

    // ---- full export/import ----

    /** Clears and reinserts everything in one atomic transaction — Import is a full replace, not a merge. */
    suspend fun restoreAll(
        businessName: String,
        currency: String,
        language: Language,
        textSize: TextSizePreset,
        categories: List<String>,
        paymentMethods: List<String>,
        stock: List<StockImport>,
        todos: List<TodoImport>,
        transactions: List<TransactionImport>,
    ) {
        db.withTransaction {
            db.settingsDao().upsert(
                SettingsEntity(
                    businessName = businessName, currency = currency, language = language, textSize = textSize,
                    hasOnboarded = true, // a restore (onboarding's Import choice or Settings' own Import) always completes onboarding
                ),
            )
            db.categoryDao().clear()
            db.categoryDao().insertAll(categories.map { CategoryEntity(name = it) })
            db.paymentMethodDao().clear()
            db.paymentMethodDao().insertAll(paymentMethods.map { PaymentMethodEntity(name = it) })
            db.stockDao().clear()
            db.stockDao().insertAll(stock.map { StockEntity(name = it.name, qty = it.qty, unit = it.unit, low = it.low) })
            db.todoDao().clear()
            db.todoDao().insertAll(todos.map { TodoEntity(title = it.title, desc = it.desc, done = it.done) })
            db.transactionDao().clear()
            db.transactionDao().insertAll(
                transactions.map {
                    TransactionEntity(
                        name = it.name, category = it.category, amount = it.amount, type = it.type,
                        method = it.method, paid = it.paid, remarks = it.remarks,
                        occurredAt = it.occurredAt, createdAt = it.createdAt, modifiedAt = it.modifiedAt,
                    )
                },
            )
        }
    }

    private fun TransactionEntity.toDomain() = Transaction(
        id = id, name = name, category = category, amount = amount, type = type,
        method = method, paid = paid, remarks = remarks,
        occurredAt = occurredAt, createdAt = createdAt, modifiedAt = modifiedAt,
    )

    private fun StockEntity.toDomain() = StockItem(id = id, name = name, qty = qty, unit = unit, low = low)

    private fun TodoEntity.toDomain() = TodoItem(id = id, title = title, desc = desc, done = done)
}
