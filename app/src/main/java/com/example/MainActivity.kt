package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CryptoType
import com.example.ui.components.CoinPriceCard
import com.example.ui.components.ConverterCard
import com.example.ui.components.CryptoHeaderBanner
import com.example.ui.components.HistorySection
import com.example.ui.theme.CryptoConverterTheme
import com.example.ui.theme.CryptoGold
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.CryptoViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: CryptoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CryptoConverterTheme {
                CryptoAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoAppScreen(viewModel: CryptoViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("crypto_app_scaffold"),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CryptoGold.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyBitcoin,
                                contentDescription = null,
                                tint = CryptoGold,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (uiState.isMalayalam) "ക്രിപ്റ്റോ കൺവേർട്ടർ" else "Crypto Converter",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "BTC • ETH • BNB ➔ INR & USD",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Hero Banner
            item {
                CryptoHeaderBanner(
                    isMalayalam = uiState.isMalayalam,
                    isRefreshing = uiState.isRefreshing,
                    lastUpdated = uiState.lastUpdated,
                    onRefresh = { viewModel.loadPrices() },
                    onToggleLanguage = { viewModel.toggleLanguage() }
                )
            }

            // Live Prices Section Header & Cards
            item {
                Column {
                    Text(
                        text = if (uiState.isMalayalam) "തൽസമയ വിലകൾ (LIVE PRICES):" else "LIVE MARKET PRICES:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CryptoGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CryptoType.values().forEach { cryptoType ->
                            val coin = uiState.coins[cryptoType] ?: com.example.data.model.CryptoCoin(
                                type = cryptoType,
                                priceUsd = cryptoType.defaultPriceUsd,
                                priceInr = cryptoType.defaultPriceInr,
                                change24hPercent = cryptoType.defaultChange24h
                            )
                            CoinPriceCard(
                                coin = coin,
                                isSelected = uiState.selectedCrypto == cryptoType,
                                isMalayalam = uiState.isMalayalam,
                                onSelect = { viewModel.selectCrypto(cryptoType) }
                            )
                        }
                    }
                }
            }

            // Converter Card
            item {
                ConverterCard(
                    mode = uiState.conversionMode,
                    selectedCrypto = uiState.selectedCrypto,
                    selectedFiat = uiState.selectedFiat,
                    inputAmountText = uiState.inputAmountText,
                    coins = uiState.coins,
                    isMalayalam = uiState.isMalayalam,
                    onToggleMode = { viewModel.toggleMode() },
                    onSelectCrypto = { viewModel.selectCrypto(it) },
                    onSelectFiat = { viewModel.selectFiat(it) },
                    onInputChange = { viewModel.updateInputText(it) },
                    onApplyPreset = { viewModel.applyPreset(it) },
                    onSaveConversion = { viewModel.saveCurrentConversion() }
                )
            }

            // History Section
            item {
                HistorySection(
                    historyList = historyList,
                    isMalayalam = uiState.isMalayalam,
                    onDelete = { viewModel.deleteHistoryItem(it) },
                    onClearAll = { viewModel.clearAllHistory() }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

