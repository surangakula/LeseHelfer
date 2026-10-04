package com.example.lesehelfer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lesehelfer.ui.ReaderViewModel
import com.example.lesehelfer.ui.theme.LeseHelferTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LeseHelferTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ReaderScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    modifier: Modifier = Modifier,
    viewModel: ReaderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val levels = listOf("A1", "A2", "B1", "B2", "C1", "C2")

    var tempApiKey by remember(uiState.apiKey) { mutableStateOf(uiState.apiKey) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Row with Title, Saved Articles Button, and Settings Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LeseHelfer 🇩🇪",
                style = MaterialTheme.typography.headlineMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { viewModel.toggleSettingsDialog(true) }) {
                    Text("⚙️ Settings")
                }
                OutlinedButton(onClick = { viewModel.toggleSavedArticlesDialog(true) }) {
                    Text("📚 Saved (${uiState.savedArticles.size})")
                }
            }
        }

        Text(
            text = "Paste a German article or story to analyze difficult words, sentence structure, and grammar tailored to your level. (Tip: For sites like DW.com that require JavaScript, simply copy & paste the article text directly).",
            style = MaterialTheme.typography.bodyMedium
        )

        // Level Selector
        Text(text = "Select CEFR Level:", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            levels.forEach { level ->
                FilterChip(
                    selected = uiState.selectedLevel == level,
                    onClick = { viewModel.updateLevel(level) },
                    label = { Text(level) }
                )
            }
        }

        // Input Text Field
        OutlinedTextField(
            value = uiState.inputText,
            onValueChange = { viewModel.updateInputText(it) },
            label = { Text("German Text / Article") },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            placeholder = { Text("Fügen Sie hier Ihren deutschen Text ein...") }
        )

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (uiState.successMessage != null) {
            Text(
                text = uiState.successMessage ?: "",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = { viewModel.analyzeText() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Analyze Text")
            }
        }

        // Analysis Results
        uiState.analysisResult?.let { result ->
            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Analysis Results", style = MaterialTheme.typography.titleMedium)
                Button(onClick = { viewModel.saveCurrentAnalysis() }) {
                    Text("💾 Save Article")
                }
            }

            Text(text = "Summary", style = MaterialTheme.typography.titleSmall)
            Text(text = result.summary, style = MaterialTheme.typography.bodyMedium)

            Text(text = "Difficult Words", style = MaterialTheme.typography.titleMedium)
            result.difficultWords.forEach { word ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "${word.word} → ${word.translation}", style = MaterialTheme.typography.titleSmall)
                        Text(text = word.explanation, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Text(text = "Grammar Points", style = MaterialTheme.typography.titleMedium)
            result.grammarPoints.forEach { gp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = gp.topic, style = MaterialTheme.typography.titleSmall)
                        Text(text = gp.explanation, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Text(text = "Sentence Breakdown", style = MaterialTheme.typography.titleMedium)
            result.translatedSentenceBreakdown.forEach { sb ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = sb.germanSentence, style = MaterialTheme.typography.bodyMedium)
                        Text(text = "EN: ${sb.englishTranslation}", style = MaterialTheme.typography.bodySmall)
                        if (sb.grammaticalNotes.isNotBlank()) {
                            Text(text = "Notes: ${sb.grammaticalNotes}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Interactive Exercises / Quiz Section
            if (result.exercises.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Interactive Exercises ✍️", style = MaterialTheme.typography.titleMedium)
                result.exercises.forEachIndexed { index, exercise ->
                    val selectedOption = uiState.userAnswers[index]
                    val isChecked = index in uiState.checkedExercises
                    val isRevealed = index in uiState.revealedExercises

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "Q${index + 1}: ${exercise.question}", style = MaterialTheme.typography.titleSmall)

                            exercise.options.forEachIndexed { optIdx, option ->
                                val isSelected = selectedOption == optIdx
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (!isChecked) viewModel.selectExerciseOption(index, optIdx)
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            if (!isChecked) viewModel.selectExerciseOption(index, optIdx)
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = option, style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.checkExerciseAnswer(index) },
                                    enabled = selectedOption != null && !isChecked
                                ) {
                                    Text("Check Answer")
                                }

                                TextButton(
                                    onClick = { viewModel.revealExerciseAnswer(index) },
                                    enabled = !isRevealed
                                ) {
                                    Text("Show Correct Answer")
                                }
                            }

                            if (isChecked) {
                                val isCorrect = selectedOption == exercise.correctAnswerIndex
                                val feedbackColor = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                val feedbackText = if (isCorrect) "✅ Correct! " else "❌ Incorrect. "
                                Text(
                                    text = "$feedbackText${exercise.explanation}",
                                    color = feedbackColor,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Settings Dialog for API Key
    if (uiState.showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleSettingsDialog(false) },
            title = { Text("Settings - OpenRouter API Key") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Enter your OpenRouter API key so the app can communicate with AI models (ideal for Obtainium builds).", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = tempApiKey,
                        onValueChange = { tempApiKey = it },
                        label = { Text("OpenRouter API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.saveApiKey(tempApiKey) }) {
                    Text("Save Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleSettingsDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Saved Articles Dialog
    if (uiState.showSavedArticlesDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleSavedArticlesDialog(false) },
            title = { Text("Saved Articles (${uiState.savedArticles.size})") },
            text = {
                if (uiState.savedArticles.isEmpty()) {
                    Text("No saved articles yet. Analyze an article and tap 'Save Article'.")
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.savedArticles.forEach { article ->
                            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(article.timestamp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectSavedArticle(article) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = article.title, style = MaterialTheme.typography.titleSmall)
                                        Text(text = "Level: ${article.level} • $dateStr", style = MaterialTheme.typography.labelSmall)
                                    }
                                    TextButton(onClick = { viewModel.deleteSavedArticle(article.id) }) {
                                        Text("Delete", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.toggleSavedArticlesDialog(false) }) {
                    Text("Close")
                }
            }
        )
    }
}
