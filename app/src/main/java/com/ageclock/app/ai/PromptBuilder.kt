package com.ageclock.app.ai

import com.ageclock.app.data.model.CalculatedAge
import com.ageclock.app.data.model.Person

/**
 * Builds context-aware messages for widget display.
 * Messages use the exact display units the user selected.
 */
object PromptBuilder {

    /**
     * Generate 5 context-aware messages based on the person's description.
     * Uses compact formatting suitable for widget display.
     */
    fun buildSimpleFallbackMessages(person: Person, age: CalculatedAge): List<String> {
        val name = person.name
        val formattedAge = age.formatCompact(person.displayUnits)
        val messages = mutableListOf<String>()

        if (age.isFuture) {
            // Countdown messages - use the user's selected format
            messages.add("$name - $formattedAge")
            messages.add("$formattedAge until $name")
            messages.add("Counting down: $formattedAge")
            messages.add("$name is $formattedAge away")
            messages.add("Looking forward to $name")
        } else {
            // Past date messages - use the user's selected format
            val description = person.description?.lowercase() ?: ""

            // Context-aware messages based on description
            when {
                description.contains("daughter") || description.contains("son") ||
                description.contains("child") || description.contains("kid") -> {
                    messages.add("$name - $formattedAge of joy")
                    messages.add("Cherish $name at $formattedAge")
                    messages.add("$formattedAge with $name")
                    messages.add("$name is growing: $formattedAge")
                    messages.add("Treasure every moment with $name")
                }
                description.contains("mom") || description.contains("dad") ||
                description.contains("parent") || description.contains("grandpa") ||
                description.contains("grandma") || description.contains("mother") ||
                description.contains("father") -> {
                    messages.add("$name - $formattedAge of wisdom")
                    messages.add("$formattedAge with $name")
                    messages.add("Cherish $name today")
                    messages.add("$name: $formattedAge of love")
                    messages.add("Visit $name while you can")
                }
                description.contains("wedding") || description.contains("anniversary") -> {
                    messages.add("$formattedAge of marriage")
                    messages.add("$name - $formattedAge together")
                    messages.add("Celebrating $formattedAge")
                    messages.add("$formattedAge of love")
                    messages.add("$name: $formattedAge strong")
                }
                else -> {
                    messages.add("$name - $formattedAge")
                    messages.add("$formattedAge with $name")
                    messages.add("$name: $formattedAge")
                    messages.add("Celebrating $name at $formattedAge")
                    messages.add("$formattedAge of memories")
                }
            }
        }

        return messages.take(5)
    }
}
