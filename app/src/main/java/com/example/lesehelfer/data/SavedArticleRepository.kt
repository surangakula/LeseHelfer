package com.example.lesehelfer.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class SavedArticleRepository(context: Context) {
    private val dir = File(context.filesDir, "saved_articles").apply { mkdirs() }
    private val json = Json { ignoreUnknownKeys = true }

    fun saveArticle(article: SavedArticle) {
        val file = File(dir, "${article.id}.json")
        file.writeText(json.encodeToString(article))
    }

    fun getSavedArticles(): List<SavedArticle> {
        return dir.listFiles()?.mapNotNull { file ->
            try {
                json.decodeFromString<SavedArticle>(file.readText())
            } catch (e: Exception) {
                null
            }
        }?.sortedByDescending { it.timestamp } ?: emptyList()
    }

    fun deleteArticle(id: String) {
        File(dir, "$id.json").delete()
    }
}
