package com.example.lesehelfer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lesehelfer.data.AnalysisResult
import com.example.lesehelfer.data.LlmProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReaderUiState(
    val inputText: String = "",
    val selectedLevel: String = "B1",
    val isLoading: Boolean = false,
    val analysisResult: AnalysisResult? = null,
    val errorMessage: String? = null
)

class ReaderViewModel(
    private val llmProvider: LlmProvider = LlmProvider()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text, errorMessage = null) }
    }

    fun updateLevel(level: String) {
        _uiState.update { it.copy(selectedLevel = level) }
    }

    fun analyzeText() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter some German text to analyze.") }
            return
        }

        val level = _uiState.value.selectedLevel

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = llmProvider.analyzeGermanText(text, level)
                _uiState.update { it.copy(isLoading = false, analysisResult = result) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Unknown error occurred") }
            }
        }
    }
}
