package com.example.smhouse.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class Command(val command: String)

data class ApiResponse(val status: String)

interface ApiService {
    @POST("api/command/")
    fun sendCommand(@Body command: Command): Call<ApiResponse>
}
