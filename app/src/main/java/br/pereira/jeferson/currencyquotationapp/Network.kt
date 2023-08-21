package br.pereira.jeferson.currencyquotationapp

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface Network {

    @GET("latest/currencies.json")
    fun getCurrencyList(): Call<ResponseBody>

    @GET("latest/currencies/{fromCurrency}/{toCurrency}.json")
    fun getCurrencyQuotation(
        @Path("fromCurrency") fromCurrency: String,
        @Path("toCurrency") toCurrency: String,
    ): Call<ResponseBody>

    companion object {
        private const val baseUrl = "https://cdn.jsdelivr.net/gh/fawazahmed0/currency-api@1/"

        fun create(): Network {
            val client = OkHttpClient.Builder().build()
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            return retrofit.create(Network::class.java)
        }
    }
}
