package com.example.googoose.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock")
data class StockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val qty: Double,
    val unit: String,
    val low: Double,
)
