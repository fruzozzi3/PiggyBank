package com.example.piggybank.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.piggybank.data.Transaction
import java.text.SimpleDateFormat
import java.util.*

// Главный экран приложения
@Composable
fun HomeScreen(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val transactions by viewModel.transactions.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf("DEPOSIT") }

    Scaffold(
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FloatingActionButton(
                    onClick = {
                        dialogType = "WITHDRAWAL"
                        showDialog = true
                    },
                    containerColor = Color(0xFFF48FB1), // Розовый
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Снять")
                }
                FloatingActionButton(
                    onClick = {
                        dialogType = "DEPOSIT"
                        showDialog = true
                    },
                    containerColor = Color(0xFF81C784), // Зеленый
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Добавить")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFF9C4), Color(0xFFF3E5F5))
                    )
                )
        ) {
            BalanceCard(
                balance = uiState.balance,
                currency = uiState.currency,
                onCurrencyChange = { newCurrency -> viewModel.changeCurrency(newCurrency) }
            )
            HistoryList(transactions = transactions, currency = uiState.currency)
        }

        TransactionDialog(
            show = showDialog,
            type = dialogType,
            onDismiss = { showDialog = false },
            onConfirm = { amount ->
                if (dialogType == "DEPOSIT") {
                    viewModel.addMoney(amount)
                } else {
                    viewModel.withdrawMoney(amount)
                }
                showDialog = false
            }
        )
    }
}

// Карточка с балансом и выбором валюты
@Composable
fun BalanceCard(balance: Double, currency: String, onCurrencyChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val currencies = listOf("UAH", "RUB", "USD")
    val currencySymbols = mapOf("UAH" to "₴", "RUB" to "₽", "USD" to "$")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Моя Копилка",
                fontSize = 20.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "${"%.2f".format(balance)} ${currencySymbols[currency]}",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Выбор валюты
            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    Text(currency)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Выбрать валюту")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    currencies.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c) },
                            onClick = {
                                onCurrencyChange(c)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// Список истории транзакций
@Composable
fun HistoryList(transactions: List<Transaction>, currency: String) {
    val currencySymbols = mapOf("UAH" to "₴", "RUB" to "₽", "USD" to "$")

    Column {
        Text(
            text = "История операций",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color.DarkGray
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(transactions) { transaction ->
                TransactionItem(transaction, currencySymbols[currency] ?: "")
            }
        }
    }
}

// Элемент списка транзакций
@Composable
fun TransactionItem(transaction: Transaction, currencySymbol: String) {
    val color = if (transaction.type == "DEPOSIT") Color(0xFF4CAF50) else Color(0xFFF44336)
    val sign = if (transaction.type == "DEPOSIT") "+" else ""
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$sign${"%.2f".format(transaction.amount)} $currencySymbol",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = color
                )
                Text(
                    text = dateFormat.format(transaction.date),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// Диалоговое окно для ввода суммы
@Composable
fun TransactionDialog(
    show: Boolean,
    type: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    val title = if (type == "DEPOSIT") "Пополнить копилку" else "Взять из копилки"
    val buttonColor = if (type == "DEPOSIT") Color(0xFF81C784) else Color(0xFFF48FB1)

    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = title, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { value ->
                            // Разрешаем ввод только чисел и одной точки
                            if (value.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                amount = value
                            }
                        },
                        label = { Text("Сумма") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val value = amount.toDoubleOrNull()
                        if (value != null && value > 0) {
                            onConfirm(value)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                ) {
                    Text("Подтвердить")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }
            }
        )
    }
}
