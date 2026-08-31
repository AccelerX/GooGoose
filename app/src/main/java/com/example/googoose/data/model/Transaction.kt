package com.example.googoose.data.model

enum class TxnType { INCOME, SPEND }

data class Transaction(
    val id: Long,
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

data class TransactionGroup(
    val date: String,
    val items: List<Transaction>,
)
