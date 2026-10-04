package com.example.lesehelfer.data

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApi {
    @POST("chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("HTTP-Referer") referer: String = "https://github.com/lesehelfer/app",
        @Header("X-Title") title: String = "LeseHelfer",
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}
