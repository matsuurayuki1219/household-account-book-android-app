package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Long,      // Represents Japanese Yen
    val type: String,      // "EXPENSE" or "INCOME"
    val category: String,  // e.g. "食費", "日用品", "給与"
    val timestamp: Long,   // Milliseconds
    val memo: String = ""
)
