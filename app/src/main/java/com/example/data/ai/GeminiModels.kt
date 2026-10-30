package com.example.data.ai

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String? = null,
    val isVoice: Boolean = false
)

enum class MessageRole {
    USER,
    MODEL,
    SYSTEM
}

enum class GeminiModel(val modelId: String, val displayName: String, val description: String) {
    FLASH("gemini-3.8-flash", "Gemini 3.8 Flash", "General store operations & advice"),
    FLASH_LITE("gemini-3.1-flash-lite", "Gemini 3.1 Flash-Lite", "Ultra-fast lookups & quick tasks"),
    ARGON("gemini-4-argon", "Gemini 4 Argon", "Deep financial & complex reasoning"),
    LIVE("gemini-3.8-flash", "Gemini 3.8 Live", "Real-time live voice conversations")
}
