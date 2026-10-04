package com.example.lesehelfer.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lesehelfer.BuildConfig
import com.example.lesehelfer.data.AnalysisResult
import com.example.lesehelfer.data.ArticleFetcher
import com.example.lesehelfer.data.LlmProvider
import com.example.lesehelfer.data.SavedArticle
import com.example.lesehelfer.data.SavedArticleRepository
import com.example.lesehelfer.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ReaderUiState(
    val inputText: String = "",
    val selectedLevel: String = "B1",
    val isLoading: Boolean = false,
    val analysisResult: AnalysisResult? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val savedArticles: List<SavedArticle> = emptyList(),
    val showSavedArticlesDialog: Boolean = false,
    val apiKey: String = "",
    val showSettingsDialog: Boolean = false,
    val userAnswers: Map<Int, Int> = emptyMap(),
    val checkedExercises: Set<Int> = emptySet(),
    val revealedExercises: Set<Int> = emptySet()
)

class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val llmProvider = LlmProvider()
    private val repository = SavedArticleRepository(application)
    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    init {
        val loadedKey = settingsRepository.getApiKey(BuildConfig.OPENROUTER_API_KEY)
        _uiState.update { it.copy(apiKey = loadedKey) }
        loadSavedArticles()
    }

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text, errorMessage = null, successMessage = null) }
    }

    fun updateLevel(level: String) {
        _uiState.update { it.copy(selectedLevel = level) }
    }

    fun saveApiKey(key: String) {
        settingsRepository.saveApiKey(key)
        _uiState.update { it.copy(apiKey = key.trim(), showSettingsDialog = false, successMessage = "API Key saved successfully!") }
    }

    fun toggleSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = show, errorMessage = null, successMessage = null) }
    }

    fun loadSavedArticles() {
        val list = repository.getSavedArticles()
        _uiState.update { it.copy(savedArticles = list) }
    }

    fun toggleSavedArticlesDialog(show: Boolean) {
        if (show) loadSavedArticles()
        _uiState.update { it.copy(showSavedArticlesDialog = show) }
    }

    fun selectExerciseOption(exerciseIndex: Int, optionIndex: Int) {
        _uiState.update {
            it.copy(userAnswers = it.userAnswers + (exerciseIndex to optionIndex))
        }
    }

    fun checkExerciseAnswer(exerciseIndex: Int) {
        _uiState.update {
            it.copy(checkedExercises = it.checkedExercises + exerciseIndex)
        }
    }

    fun revealExerciseAnswer(exerciseIndex: Int) {
        val exercise = _uiState.value.analysisResult?.exercises?.getOrNull(exerciseIndex) ?: return
        _uiState.update {
            it.copy(
                userAnswers = it.userAnswers + (exerciseIndex to exercise.correctAnswerIndex),
                checkedExercises = it.checkedExercises + exerciseIndex,
                revealedExercises = it.revealedExercises + exerciseIndex
            )
        }
    }

    fun saveCurrentAnalysis() {
        val state = _uiState.value
        val analysis = state.analysisResult ?: return
        val text = state.inputText.trim()
        if (text.isBlank()) return

        val title = analysis.summary.take(50) + if (analysis.summary.length > 50) "..." else ""
        val article = SavedArticle(
            id = UUID.randomUUID().toString(),
            title = title,
            text = text,
            level = state.selectedLevel,
            timestamp = System.currentTimeMillis(),
            analysis = analysis
        )

        repository.saveArticle(article)
        loadSavedArticles()
        _uiState.update { it.copy(successMessage = "Article saved successfully!") }
    }

    fun selectSavedArticle(article: SavedArticle) {
        _uiState.update {
            it.copy(
                inputText = article.text,
                selectedLevel = article.level,
                analysisResult = article.analysis,
                showSavedArticlesDialog = false,
                errorMessage = null,
                successMessage = "Loaded saved article.",
                userAnswers = emptyMap(),
                checkedExercises = emptySet(),
                revealedExercises = emptySet()
            )
        }
    }

    fun deleteSavedArticle(id: String) {
        repository.deleteArticle(id)
        loadSavedArticles()
    }

    fun analyzeText() {
        val state = _uiState.value
        val rawInput = state.inputText.trim()
        if (rawInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter German text or a valid article URL.") }
            return
        }

        val level = state.selectedLevel
        val currentKey = state.apiKey

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null, userAnswers = emptyMap(), checkedExercises = emptySet(), revealedExercises = emptySet()) }
            try {
                val textToAnalyze = if (rawInput.startsWith("http://") || rawInput.startsWith("https://")) {
                    try {
                        val fetched = ArticleFetcher.fetchTextFromUrl(rawInput)
                        if (fetched.isBlank()) throw IllegalStateException("Could not extract text from URL.")
                        fetched
                    } catch (e: Exception) {
                        throw IllegalStateException("Failed to fetch URL: ${e.localizedMessage}")
                    }
                } else {
                    rawInput
                }

                val result = llmProvider.analyzeGermanText(textToAnalyze, level, currentKey)
                _uiState.update { it.copy(isLoading = false, inputText = textToAnalyze, analysisResult = result) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Unknown error occurred") }
            }
        }
    }
}
