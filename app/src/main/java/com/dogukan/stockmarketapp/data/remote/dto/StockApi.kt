package com.dogukan.stockmarketapp.data.remote.dto

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface StockApi {

    @GET("query?function=LISTING_STATUS")
    suspend fun getListing(
        @Query("apikey") apiKey : String = API_KEY
    ) : ResponseBody

    companion object{
        const val API_KEY = "UHYUQBP179R7W8PS"
        const val BASE_URL = "https://alphavantage.co"
    }
}