package com.example.googoose.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Query("SELECT * FROM stock ORDER BY id ASC")
    fun observeAll(): Flow<List<StockEntity>>

    @Query("SELECT * FROM stock WHERE id = :id")
    suspend fun findById(id: Long): StockEntity?

    @Insert
    suspend fun insert(entity: StockEntity): Long

    @Update
    suspend fun update(entity: StockEntity)

    @Query("DELETE FROM stock WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert
    suspend fun insertAll(entities: List<StockEntity>)

    @Query("DELETE FROM stock")
    suspend fun clear()
}
