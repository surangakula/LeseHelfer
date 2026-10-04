package com.example.lesehelfer.data

import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequest(
    val model: String = "openai/gpt-4o-mini",
    val messages: List<ChatMessage>,
    val response_format: ResponseFormat? = null
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ResponseFormat(
    val type: String = "json_object"
)

@Serializable
data class ChatCompletionResponse(
    val choices: List<Choice>
)

@Serializable
data class Choice(
    val message: ChatMessage
)

@Serializable
data class AnalysisResult(
    val summary: String,
    val difficultWords: List<DifficultWord>,
    val grammarPoints: List<GrammarPoint>,
    val translatedSentenceBreakdown: List<SentenceBreakdown>
)

@Serializable
data class DifficultWord(
    val word: String,
    val translation: String,
    val explanation: String
)

@Serializable
data class GrammarPoint(
    val topic: String,
    val explanation: String
)

@Serializable
data class SentenceBreakdown(
    val germanSentence: String,
    val englishTranslation: String,
    val grammaticalNotes: String
)
