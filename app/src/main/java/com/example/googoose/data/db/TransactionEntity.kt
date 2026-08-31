package com.example.googoose.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.googoose.data.model.TxnType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val amount: Double,
    val type: TxnType,
    val method: String,
    val paid: Boolean,
    val remarks: String,
    /** User-facing "Date" field from the Add-transaction sheet. */
    val occurredAt: Long,
    val createdAt: Long,
    val modifiedAt: Long,
)
