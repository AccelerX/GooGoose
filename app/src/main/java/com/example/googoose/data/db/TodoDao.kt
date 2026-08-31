package com.example.googoose.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    /** id ASC == insertion order — stands in for the mockup's `origIndex`. */
    @Query("SELECT * FROM todos ORDER BY id ASC")
    fun observeAll(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun findById(id: Long): TodoEntity?

    @Insert
    suspend fun insert(entity: TodoEntity): Long

    @Update
    suspend fun update(entity: TodoEntity)

    @Insert
    suspend fun insertAll(entities: List<TodoEntity>)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM todos")
    suspend fun clear()
}
