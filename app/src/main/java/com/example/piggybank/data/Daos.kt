package com.example.piggybank.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    // Вставить новую транзакцию
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: Transaction)

    // Получить все транзакции, отсортированные по дате (сначала новые)
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>
}

@Dao
interface PiggyBankDao {
    // Вставить или обновить информацию о копилке
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(piggyBank: PiggyBank)

    // Получить информацию о копилке
    @Query("SELECT * FROM piggy_bank WHERE id = 1")
    fun getPiggyBank(): Flow<PiggyBank?>
}
