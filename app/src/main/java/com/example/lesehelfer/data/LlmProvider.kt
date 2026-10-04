package com.example.lesehelfer.data

import com.example.lesehelfer.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
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

    suspend fun analyzeGermanText(text: String, cefrLevel: String): AnalysisResult {
        val apiKey = BuildConfig.OPENROUTER_API_KEY
        if (apiKey.isBlank()) {
            throw IllegalStateException("OpenRouter API key is missing. Please set OPENROUTER_API_KEY in local.properties.")
        }

        val prompt = """
            You are an expert German language tutor for CEFR level $cefrLevel.
            Analyze the following German text. Return a JSON object strictly matching this schema:
            {
              "summary": "Brief summary in English",
              "difficultWords": [{"word": "German word", "translation": "English translation", "explanation": "Brief usage explanation"}],
              "grammarPoints": [{"topic": "Grammar topic name", "explanation": "Explanation tailored to $cefrLevel level"}],
              "translatedSentenceBreakdown": [{"germanSentence": "Sentence in German", "englishTranslation": "English translation", "grammaticalNotes": "Notes"}]
            }
            
            German text to analyze:
            $text
        """.trimIndent()

        val request = ChatCompletionRequest(
            model = "openai/gpt-4o-mini",
            messages = listOf(
                ChatMessage(role = "system", content = "You are a helpful German language learning assistant that responds only in valid JSON."),
                ChatMessage(role = "user", content = prompt)
            ),
            response_format = ResponseFormat(type = "json_object")
        )

        val response = api.getChatCompletion(
            authorization = "Bearer $apiKey",
            request = request
        )

        val content = response.choices.firstOrNull()?.message?.content 
            ?: throw IllegalStateException("Empty response from LLM")

        return json.decodeFromString<AnalysisResult>(content)
    }
}
