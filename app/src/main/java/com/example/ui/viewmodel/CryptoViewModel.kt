package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ConversionHistory
import com.example.data.model.CryptoCoin
import com.example.data.model.CryptoType
import com.example.data.model.CryptoRepository
import com.example.data.model.FiatCurrency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.Exception

enum class ConversionMode {
    CRYPTO_TO_FIAT,
    FIAT_TO_CRYPTO
}

data class CryptoUiState(
    val coins: Map<CryptoType, CryptoCoin> = emptyMap(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis(),
    val isMalayalam: Boolean = false,
    val selectedCrypto: CryptoType = CryptoType.BTC,
    val selectedFiat: FiatCurrency = FiatCurrency.INR,
    val conversionMode: ConversionMode = ConversionMode.CRYPTO_TO_FIAT,
    val inputAmountText: String = "1",
    val snackbarMessage: String? = null
)

class CryptoViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = CryptoRepository(historyDao = database.conversionHistoryDao())

    private val _uiState = MutableStateFlow(CryptoUiState())
    val uiState: StateFlow<CryptoUiState> = _uiState.asStateFlow()

    val historyList: StateFlow<List<ConversionHistory>> = repository.allHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadPrices()
    }

    fun loadPrices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val fetchedMap = repository.fetchLivePrices()
            _uiState.update {
                it.copy(
                    coins = fetchedMap,
                    isLoading = false,
                    isRefreshing = false,
                    lastUpdated = System.currentTimeMillis()
                )
            }
        }
    }

    fun toggleLanguage() {
        _uiState.update { it.copy(isMalayalam = !it.isMalayalam) }
    }

    fun selectCrypto(type: CryptoType) {
        _uiState.update { it.copy(selectedCrypto = type) }
    }

    fun selectFiat(fiat: FiatCurrency) {
        _uiState.update { it.copy(selectedFiat = fiat) }
    }

    fun toggleMode() {
        _uiState.update {
            val newMode = if (it.conversionMode == ConversionMode.CRYPTO_TO_FIAT) {
                ConversionMode.FIAT_TO_CRYPTO
            } else {
                ConversionMode.CRYPTO_TO_FIAT
            }
            // Reset input text to a sensible default when swapping mode
            val defaultInput = if (newMode == ConversionMode.CRYPTO_TO_FIAT) "1" else "10000"
            it.copy(conversionMode = newMode, inputAmountText = defaultInput)
        }
    }

    fun updateInputText(input: String) {
        // Only allow numbers and decimal point
        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.update { it.copy(inputAmountText = input) }
        }
    }

    fun applyPreset(value: Double) {
        val text = if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            value.toString()
        }
        _uiState.update { it.copy(inputAmountText = text) }
    }

    fun saveCurrentConversion() {
        val state = uiState.value
        val amount = state.inputAmountText.toDoubleOrNull() ?: return
        if (amount <= 0) return

        val currentCoin = state.coins[state.selectedCrypto] ?: return

        val (cryptoAmt, inrVal, usdVal) = if (state.conversionMode == ConversionMode.CRYPTO_TO_FIAT) {
            val inr = amount * currentCoin.priceInr
            val usd = amount * currentCoin.priceUsd
            Triple(amount, inr, usd)
        } else {
            // Fiat to crypto
            val isInr = state.selectedFiat == FiatCurrency.INR
            val inr = if (isInr) amount else amount * (currentCoin.priceInr / currentCoin.priceUsd)
            val usd = if (!isInr) amount else amount * (currentCoin.priceUsd / currentCoin.priceInr)
            val crypto = if (isInr) amount / currentCoin.priceInr else amount / currentCoin.priceUsd
            Triple(crypto, inr, usd)
        }

        val record = ConversionHistory(
            cryptoSymbol = state.selectedCrypto.symbol,
            cryptoAmount = cryptoAmt,
            fiatCode = state.selectedFiat.code,
            fiatAmount = if (state.conversionMode == ConversionMode.FIAT_TO_CRYPTO) amount else inrVal,
            convertedInr = inrVal,
            convertedUsd = usdVal
        )

        viewModelScope.launch {
            try {
                repository.saveConversion(record)
                val msg = if (state.isMalayalam) "കൺവേർഷൻ സേവ് ചെയ്തു!" else "Conversion saved to history!"
                _uiState.update { it.copy(snackbarMessage = msg) }
            } catch (e: Exception) {
                // error handling
            }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
