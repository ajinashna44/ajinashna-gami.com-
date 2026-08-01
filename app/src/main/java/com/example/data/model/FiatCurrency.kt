package com.example.data.model

enum class FiatCurrency(
    val code: String,
    val symbol: String,
    val currencyName: String,
    val currencyNameMl: String
) {
    INR("INR", "₹", "Indian Rupee", "ഇന്ത്യൻ രൂപ"),
    USD("USD", "$", "US Dollar", "യുഎസ് ഡോളർ")
}
