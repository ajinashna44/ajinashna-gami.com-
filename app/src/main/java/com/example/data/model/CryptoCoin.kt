package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.BnbColor
import com.example.ui.theme.BtcColor
import com.example.ui.theme.EthColor

enum class CryptoType(
    val symbol: String,
    val coinName: String,
    val coinNameMl: String,
    val brandColor: Color,
    val defaultPriceUsd: Double,
    val defaultPriceInr: Double,
    val defaultChange24h: Double
) {
    BTC(
        symbol = "BTC",
        coinName = "Bitcoin",
        coinNameMl = "ബിറ്റ്കോയിൻ (BTC)",
        brandColor = BtcColor,
        defaultPriceUsd = 96450.00,
        defaultPriceInr = 8150025.00,
        defaultChange24h = +3.42
    ),
    ETH(
        symbol = "ETH",
        coinName = "Ethereum",
        coinNameMl = "എഥീറിയം (ETH)",
        brandColor = EthColor,
        defaultPriceUsd = 3480.50,
        defaultPriceInr = 294100.00,
        defaultChange24h = +1.85
    ),
    BNB(
        symbol = "BNB",
        coinName = "Binance Coin",
        coinNameMl = "ബിഎൻബി (BNB)",
        brandColor = BnbColor,
        defaultPriceUsd = 655.20,
        defaultPriceInr = 55365.00,
        defaultChange24h = -0.75
    )
}

data class CryptoCoin(
    val type: CryptoType,
    val priceUsd: Double,
    val priceInr: Double,
    val change24hPercent: Double,
    val high24hUsd: Double = priceUsd * 1.03,
    val low24hUsd: Double = priceUsd * 0.97
)
