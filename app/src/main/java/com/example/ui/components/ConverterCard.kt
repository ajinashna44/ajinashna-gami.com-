package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoCoin
import com.example.data.model.CryptoType
import com.example.data.model.FiatCurrency
import com.example.ui.theme.CryptoGold
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ConversionMode
import java.text.DecimalFormat

@OptIn(ExperimentalAnimationApi::class, ExperimentalLayoutApi::class)
@Composable
fun ConverterCard(
    mode: ConversionMode,
    selectedCrypto: CryptoType,
    selectedFiat: FiatCurrency,
    inputAmountText: String,
    coins: Map<CryptoType, CryptoCoin>,
    isMalayalam: Boolean,
    onToggleMode: () -> Unit,
    onSelectCrypto: (CryptoType) -> Unit,
    onSelectFiat: (FiatCurrency) -> Unit,
    onInputChange: (String) -> Unit,
    onApplyPreset: (Double) -> Unit,
    onSaveConversion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coin = coins[selectedCrypto] ?: CryptoCoin(
        type = selectedCrypto,
        priceUsd = selectedCrypto.defaultPriceUsd,
        priceInr = selectedCrypto.defaultPriceInr,
        change24hPercent = selectedCrypto.defaultChange24h
    )

    val inputVal = inputAmountText.toDoubleOrNull() ?: 0.0

    // Computations
    val computedInr: Double
    val computedUsd: Double
    val computedCrypto: Double

    if (mode == ConversionMode.CRYPTO_TO_FIAT) {
        computedInr = inputVal * coin.priceInr
        computedUsd = inputVal * coin.priceUsd
        computedCrypto = inputVal
    } else {
        // Fiat to Crypto
        if (selectedFiat == FiatCurrency.INR) {
            computedInr = inputVal
            computedUsd = inputVal / (coin.priceInr / coin.priceUsd)
            computedCrypto = if (coin.priceInr > 0) inputVal / coin.priceInr else 0.0
        } else {
            computedUsd = inputVal
            computedInr = inputVal * (coin.priceInr / coin.priceUsd)
            computedCrypto = if (coin.priceUsd > 0) inputVal / coin.priceUsd else 0.0
        }
    }

    val decimalInr = DecimalFormat("#,##0.00")
    val decimalUsd = DecimalFormat("#,##0.00")
    val decimalCrypto = DecimalFormat("#,##0.000000")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("converter_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Conversion Mode Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground, RoundedCornerShape(12.dp))
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Crypto -> Fiat Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (mode == ConversionMode.CRYPTO_TO_FIAT) CryptoGold else Color.Transparent
                        )
                        .clickable { if (mode != ConversionMode.CRYPTO_TO_FIAT) onToggleMode() }
                        .padding(vertical = 10.dp)
                        .testTag("mode_tab_crypto_to_fiat"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isMalayalam) "ക്രിപ്റ്റോ ➔ ഫിയറ്റ്" else "Crypto ➔ Fiat",
                        color = if (mode == ConversionMode.CRYPTO_TO_FIAT) DarkBackground else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Fiat -> Crypto Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (mode == ConversionMode.FIAT_TO_CRYPTO) CryptoGold else Color.Transparent
                        )
                        .clickable { if (mode != ConversionMode.FIAT_TO_CRYPTO) onToggleMode() }
                        .padding(vertical = 10.dp)
                        .testTag("mode_tab_fiat_to_crypto"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isMalayalam) "ഫിയറ്റ് ➔ ക്രിപ്റ്റോ" else "Fiat ➔ Crypto",
                        color = if (mode == ConversionMode.FIAT_TO_CRYPTO) DarkBackground else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Crypto Coin Selection Row
            Text(
                text = if (isMalayalam) "ക്രിപ്റ്റോകറൻസി തിരഞ്ഞെടുക്കുക:" else "Select Cryptocurrency:",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CryptoType.values().forEach { crypto ->
                    val isSelected = crypto == selectedCrypto
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectCrypto(crypto) }
                            .testTag("crypto_chip_${crypto.symbol.lowercase()}"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) crypto.brandColor.copy(alpha = 0.2f) else DarkSurfaceVariant,
                        border = BorderStroke(1.dp, if (isSelected) crypto.brandColor else DarkCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(crypto.brandColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = crypto.symbol,
                                color = if (isSelected) crypto.brandColor else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // If in FIAT_TO_CRYPTO mode, select Fiat Currency (INR vs USD)
            if (mode == ConversionMode.FIAT_TO_CRYPTO) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isMalayalam) "ഫിയറ്റ് കറൻസി തിരഞ്ഞെടുക്കുക:" else "Select Fiat Currency:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FiatCurrency.values().forEach { fiat ->
                        val isSelected = fiat == selectedFiat
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectFiat(fiat) }
                                .testTag("fiat_chip_${fiat.code.lowercase()}"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) CryptoGold.copy(alpha = 0.2f) else DarkSurfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) CryptoGold else DarkCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${fiat.symbol} ${fiat.code}",
                                    color = if (isSelected) CryptoGold else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input Section
            Text(
                text = if (mode == ConversionMode.CRYPTO_TO_FIAT) {
                    if (isMalayalam) "ക്രിപ്റ്റോ അളവ് നൽകുക (${selectedCrypto.symbol}):" else "Enter Amount (${selectedCrypto.symbol}):"
                } else {
                    if (isMalayalam) "ഫിയറ്റ് തുക നൽകുക (${selectedFiat.symbol}):" else "Enter Fiat Amount (${selectedFiat.symbol}):"
                },
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = inputAmountText,
                onValueChange = { onInputChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("converter_input_field"),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    if (inputAmountText.isNotEmpty()) {
                        IconButton(onClick = { onInputChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = TextMuted
                            )
                        }
                    }
                },
                leadingIcon = {
                    Text(
                        text = if (mode == ConversionMode.CRYPTO_TO_FIAT) selectedCrypto.symbol else selectedFiat.symbol,
                        color = CryptoGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CryptoGold,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedContainerColor = DarkBackground,
                    unfocusedContainerColor = DarkBackground,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true,
                placeholder = {
                    Text(text = "0.00", color = TextMuted)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Preset Quick Buttons Row
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (mode == ConversionMode.CRYPTO_TO_FIAT) {
                    listOf(0.01, 0.1, 0.5, 1.0, 5.0).forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DarkSurfaceVariant,
                            border = BorderStroke(1.dp, DarkCardBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onApplyPreset(preset) }
                                .testTag("preset_${preset}")
                        ) {
                            Text(
                                text = "+$preset ${selectedCrypto.symbol}",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                } else {
                    val presets = if (selectedFiat == FiatCurrency.INR) listOf(1000.0, 5000.0, 10000.0, 50000.0, 100000.0)
                    else listOf(50.0, 100.0, 500.0, 1000.0, 5000.0)

                    presets.forEach { preset ->
                        val formattedPreset = if (selectedFiat == FiatCurrency.INR) "₹${preset.toInt()}" else "$${preset.toInt()}"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DarkSurfaceVariant,
                            border = BorderStroke(1.dp, DarkCardBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onApplyPreset(preset) }
                                .testTag("preset_${preset.toInt()}")
                        ) {
                            Text(
                                text = formattedPreset,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Center Swap Direction Button
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Divider(
                    color = DarkCardBorder,
                    modifier = Modifier.fillMaxWidth()
                )
                IconButton(
                    onClick = onToggleMode,
                    modifier = Modifier
                        .background(DarkBackground, CircleShape)
                        .border(1.dp, CryptoGold, CircleShape)
                        .size(36.dp)
                        .testTag("swap_mode_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap Mode",
                        tint = CryptoGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Output Results Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("conversion_result_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkBackground),
                border = BorderStroke(1.dp, CryptoGold.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (isMalayalam) "കൺവേർഷൻ ഫലം (CONVERSION RESULT):" else "CONVERSION RESULT:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CryptoGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AnimatedContent(
                        targetState = mode,
                        transitionSpec = { fadeIn() with fadeOut() },
                        label = "result_animation"
                    ) { currentMode ->
                        if (currentMode == ConversionMode.CRYPTO_TO_FIAT) {
                            Column {
                                // Primary INR Output
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isMalayalam) "ഇന്ത്യൻ രൂപ (INR)" else "Indian Rupee (INR)",
                                        color = TextSecondary,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "₹${decimalInr.format(computedInr)}",
                                        color = CryptoGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Secondary USD Output
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isMalayalam) "യുഎസ് ഡോളർ (USD)" else "US Dollar (USD)",
                                        color = TextSecondary,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "$${decimalUsd.format(computedUsd)}",
                                        color = CryptoGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                }
                            }
                        } else {
                            // Fiat to Crypto Output
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${selectedCrypto.symbol} (${selectedCrypto.coinName})",
                                        color = TextSecondary,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${decimalCrypto.format(computedCrypto)} ${selectedCrypto.symbol}",
                                        color = selectedCrypto.brandColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (selectedFiat == FiatCurrency.INR) "USD Equivalent" else "INR Equivalent",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = if (selectedFiat == FiatCurrency.INR) "$${decimalUsd.format(computedUsd)}" else "₹${decimalInr.format(computedInr)}",
                                        color = TextSecondary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: Copy & Save History
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Copy Button
                OutlinedButton(
                    onClick = {
                        val resultText = if (mode == ConversionMode.CRYPTO_TO_FIAT) {
                            "$inputVal ${selectedCrypto.symbol} = ₹${decimalInr.format(computedInr)} / $${decimalUsd.format(computedUsd)}"
                        } else {
                            "${selectedFiat.symbol}$inputVal = ${decimalCrypto.format(computedCrypto)} ${selectedCrypto.symbol}"
                        }
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Crypto Conversion", resultText))
                        Toast.makeText(context, if (isMalayalam) "ക്ലിപ്പ്ബോർഡിലേക്ക് കോപ്പി ചെയ്തു!" else "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("copy_result_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DarkCardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMalayalam) "കോപ്പി" else "Copy",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Save to History Button
                Button(
                    onClick = onSaveConversion,
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("save_conversion_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CryptoGold,
                        contentColor = DarkBackground
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Save",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMalayalam) "സേവ് ചെയ്യുക" else "Save Record",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
