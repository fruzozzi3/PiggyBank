package com.example.piggybank.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.piggybank.data.AppDatabase
import com.example.piggybank.data.PiggyBank
import com.example.piggybank.data.Transaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

 ViewModel для управления состоянием UI и бизнес-логикой
class MainViewModel(application Application)  AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()
    private val piggyBankDao = db.piggyBankDao()

     StateFlow для UI, чтобы он автоматически обновлялся при изменении данных
    val uiState StateFlowPiggyBankUiState = piggyBankDao.getPiggyBank()
        .map { piggyBank -
             Если копилки нет, создаем новую с нулевым балансом
            if (piggyBank == null) {
                val initialPiggy = PiggyBank(balance = 0.0, currency = UAH)
                piggyBankDao.upsert(initialPiggy)
                PiggyBankUiState(initialPiggy.balance, initialPiggy.currency)
            } else {
                PiggyBankUiState(piggyBank.balance, piggyBank.currency)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PiggyBankUiState()
        )

    val transactions = transactionDao.getAllTransactions()

     Функция для добавления денег
    fun addMoney(amount Double) {
        viewModelScope.launch {
            val currentBalance = uiState.value.balance
            val newBalance = currentBalance + amount
            val transaction = Transaction(amount = amount, type = DEPOSIT, date = Date())

            transactionDao.insert(transaction)
            piggyBankDao.upsert(PiggyBank(balance = newBalance, currency = uiState.value.currency))
        }
    }

     Функция для снятия денег
    fun withdrawMoney(amount Double) {
        viewModelScope.launch {
            val currentBalance = uiState.value.balance
            if (currentBalance = amount) {
                val newBalance = currentBalance - amount
                val transaction = Transaction(amount = -amount, type = WITHDRAWAL, date = Date())

                transactionDao.insert(transaction)
                piggyBankDao.upsert(PiggyBank(balance = newBalance, currency = uiState.value.currency))
            }
        }
    }

     Функция для смены валюты
    fun changeCurrency(newCurrency String) {
        viewModelScope.launch {
            piggyBankDao.upsert(PiggyBank(balance = uiState.value.balance, currency = newCurrency))
        }
    }
}

 Класс для представления состояния UI
data class PiggyBankUiState(
    val balance Double = 0.0,
    val currency String = UAH
)
