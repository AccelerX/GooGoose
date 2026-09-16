package com.example.googoose.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Mirrors [CategoryDao] exactly — same shape, different table. */
@Dao
interface PaymentMethodDao {
    @Query("SELECT * FROM payment_methods ORDER BY id ASC")
    fun observeAll(): Flow<List<PaymentMethodEntity>>

    @Insert
    suspend fun insert(entity: PaymentMethodEntity)

    @Insert
    suspend fun insertAll(entities: List<PaymentMethodEntity>)

    @Query("DELETE FROM payment_methods WHERE name = :name")
    suspend fun deleteByName(name: String)

    @Query("SELECT EXISTS(SELECT 1 FROM payment_methods WHERE name = :name)")
    suspend fun exists(name: String): Boolean

    @Query("DELETE FROM payment_methods")
    suspend fun clear()
}
