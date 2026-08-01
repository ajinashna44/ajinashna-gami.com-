package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversion_history")
data class ConversionHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cryptoSymbol: String,
    val cryptoAmount: Double,
    val fiatCode: String,
    val fiatAmount: Double,
    val convertedInr: Double,
    val convertedUsd: Double,
    val timestamp: Long = System.currentTimeMillis()
)
