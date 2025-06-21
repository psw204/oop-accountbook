package com.example.project1

data class TransactionModel(
    val id: String? = null,
    val description: String? = null,
    val amount: Double? = null,
    val type: String? = null, // "income" or "expense"
    val timestamp: Long? = null
) 