package com.example.lesehelfer.data

import com.example.lesehelfer.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.HttpException
import retrofit2.Retrofit

class LlmProvider {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private val api: OpenRouterApi by lazy {
        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://openrouter.ai/api/v1/")
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
        retrofit.create(OpenRouterApi::class.java)
    }

    suspend fun analyzeGermanText(text: String, cefrLevel: String, apiKeyOverride: String? = null): AnalysisResult {
        val apiKey = if (!apiKeyOverride.isNullOrBlank()) apiKeyOverride else BuildConfig.OPENROUTER_API_KEY
        if (apiKey.isBlank()) {
            throw IllegalStateException("OpenRouter API key is missing. Please set it in Settings.")
        }

        val prompt = """
            You are an expert German language tutor for CEFR level $cefrLevel.
            Analyze the following German text. Return a JSON object strictly matching this schema:
            {
              "summary": "Brief summary in English",
              "difficultWords": [{"word": "German word", "translation": "English translation", "explanation": "Brief usage explanation"}],
              "grammarPoints": [{"topic": "Grammar topic name", "explanation": "Explanation tailored to $cefrLevel level"}],
              "translatedSentenceBreakdown": [{"germanSentence": "Sentence in German", "englishTranslation": "English translation", "grammaticalNotes": "Notes"}],
              "exercises": [{"question": "Quiz question based on grammar/vocab", "options": ["A", "B", "C", "D"], "correctAnswerIndex": 0, "explanation": "Explanation"}]
            }
            
            German text to analyze:
            $text
        """.trimIndent()

        val request = ChatCompletionRequest(
            model = "openrouter/auto",
            messages = listOf(
                ChatMessage(role = "system", content = "You are a helpful German language learning assistant. Always respond with valid JSON only, no markdown formatting."),
                ChatMessage(role = "user", content = prompt)
            )
        )

        val response = try {
            api.getChatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: e.message()
            throw IllegalStateException("OpenRouter Error (${e.code()}): $errorBody")
        }

        val content = response.choices.firstOrNull()?.message?.content 
            ?: throw IllegalStateException("Empty response from LLM")

        val cleanedContent = content.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return json.decodeFromString<AnalysisResult>(cleanedContent)
    }
}
