package com.example.lesehelfer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lesehelfer.ui.ReaderViewModel
import com.example.lesehelfer.ui.theme.LeseHelferTheme

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "LeseHelfer 🇩🇪",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Paste a German article, story, or chapter to analyze difficult words, sentence structure, and grammar tailored to your level.",
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

            Text(text = "Summary", style = MaterialTheme.typography.titleMedium)
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
        }
    }
}
