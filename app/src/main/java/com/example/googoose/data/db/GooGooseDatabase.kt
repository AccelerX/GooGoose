package com.example.googoose.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        TransactionEntity::class,
        StockEntity::class,
        TodoEntity::class,
        CategoryEntity::class,
        SettingsEntity::class,
        PaymentMethodEntity::class,
    ],
    version = 4, // v4: SettingsEntity gained `hasOnboarded` — dev-only schema change, fallbackToDestructiveMigration handles it.
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class GooGooseDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun stockDao(): StockDao
    abstract fun todoDao(): TodoDao
    abstract fun categoryDao(): CategoryDao
    abstract fun settingsDao(): SettingsDao
    abstract fun paymentMethodDao(): PaymentMethodDao
}
