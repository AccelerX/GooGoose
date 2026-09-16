package com.example.googoose.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Payment-method autocomplete history for the Add-transaction sheet. Mirrors [CategoryEntity]'s shape. */
@Entity(tableName = "payment_methods", indices = [Index(value = ["name"], unique = true)])
data class PaymentMethodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)
