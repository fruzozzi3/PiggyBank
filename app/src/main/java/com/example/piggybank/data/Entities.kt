package com.example.piggybank.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

// Сущность для транзакции (пополнение или снятие)
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val type: String, // "DEPOSIT" or "WITHDRAWAL"
    val date: Date,
    val description: String = ""
)

// Сущность для хранения информации о копилке (баланс и валюта)
@Entity(tableName = "piggy_bank")
data class PiggyBank(
    @PrimaryKey val id: Int = 1, // Всегда один и тот же ID для единственной копилки
    val balance: Double,
    val currency: String // "UAH", "RUB", "USD"
)
