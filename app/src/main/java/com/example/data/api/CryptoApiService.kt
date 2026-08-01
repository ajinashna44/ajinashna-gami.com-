package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class CoinPriceDetail(
    @Json(name = "usd") val usd: Double?,
    @Json(name = "inr") val inr: Double?,
    @Json(name = "usd_24h_change") val usd24hChange: Double?
)

data class CoinGeckoResponse(
    @Json(name = "bitcoin") val bitcoin: CoinPriceDetail?,
    @Json(name = "ethereum") val ethereum: CoinPriceDetail?,
    @Json(name = "binancecoin") val binancecoin: CoinPriceDetail?
)

interface CryptoApiService {
    @GET("api/v3/simple/price")
    suspend fun getSimplePrices(
        @Query("ids") ids: String = "bitcoin,ethereum,binancecoin",
        @Query("vs_currencies") vsCurrencies: String = "usd,inr",
        @Query("include_24hr_change") include24hChange: Boolean = true
    ): CoinGeckoResponse

    companion object {
        private const val BASE_URL = "https://api.coingecko.com/"

        fun create(): CryptoApiService {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            return retrofit.create(CryptoApiService::class.java)
        }
    }
}
