# LeseHelfer - Copilot Instructions

## Project Overview
**LeseHelfer** is an Android application built with **Jetpack Compose** and **Kotlin** designed to help German learners (levels A1 to C2) analyze articles, stories, or chapters from books (e.g., from DW.com or literature). 

The app uses a **Cloud-first AI approach (OpenRouter API)** to:
- Identify difficult vocabulary and provide definitions/translations.
- Breakdown complex sentences.
- Explain grammar structures tailored to the user's German proficiency level (A1-C2).

---

## Tech Stack & Architecture
- **UI Toolkit**: Jetpack Compose with Material 3.
- **Architecture**: MVVM (Model-View-ViewModel) with StateFlow.
- **Networking**: Retrofit with `kotlinx.serialization` for JSON request/response handling.
- **AI Backend**: OpenRouter API (`https://openrouter.ai/api/v1/`).
- **Configuration**: API keys stored securely in `local.properties` and injected via Gradle `buildConfigField`.

---

## Coding Standards & Guidelines
1. **Idiomatic Kotlin**: Use coroutines (`viewModelScope`, `suspend` functions), Flows, and immutable data classes.
2. **Compose Best Practices**: Stateless composables hoisting state up to ViewModels; use `Material3` design tokens.
3. **Error Handling**: Gracefully handle network errors, rate limits, and JSON parsing issues from LLM responses.
4. **Clean Code**: Keep classes focused, modular, and testable.
