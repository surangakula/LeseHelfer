package com.example.lesehelfer.data

import kotlinx.serialization.Serializable

@Serializable
data class SavedArticle(
    val id: String,
    val title: String,
    val text: String,
    val level: String,
    val timestamp: Long,
    val analysis: AnalysisResult
)
