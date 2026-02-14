package com.ageclock.app.ai

import android.content.Context
import com.ageclock.app.data.model.CalculatedAge
import com.ageclock.app.data.model.Person
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * MessageGenerator handles LLM inference for generating personalized widget messages.
 *
 * This implementation uses a fallback approach - when the on-device LLM is not available
 * or fails, it falls back to template-based messages from PromptBuilder.
 *
 * The actual LLM integration depends on the llama.cpp library API which may vary.
 * For now, we use the fallback messages as a reliable baseline.
 */
class MessageGenerator(private val context: Context) {
    private var isInitialized = false

    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!ModelManager.isModelDownloaded(context)) {
                return@withContext Result.failure(IllegalStateException("Model not downloaded"))
            }

            // TODO: Initialize the actual LLM when the library API is confirmed
            // For now, we mark as initialized to enable fallback messages
            isInitialized = true

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateMessages(person: Person, age: CalculatedAge): List<String> = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            return@withContext PromptBuilder.buildSimpleFallbackMessages(person, age)
        }

        try {
            // TODO: Implement actual LLM inference when library API is confirmed
            // For now, use the high-quality fallback messages
            PromptBuilder.buildSimpleFallbackMessages(person, age)
        } catch (e: Exception) {
            // Fall back to simple messages on any error
            PromptBuilder.buildSimpleFallbackMessages(person, age)
        }
    }

    fun release() {
        isInitialized = false
    }

    fun isReady(): Boolean = isInitialized
}
