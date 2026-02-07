package com.ageclock.app.ai

import com.ageclock.app.data.model.AgeUnits
import com.ageclock.app.data.model.CalculatedAge
import com.ageclock.app.data.model.Person

object PromptBuilder {

    fun buildPrompt(person: Person, age: CalculatedAge): String {
        val name = person.name
        val description = person.description ?: ""
        val type = if (age.isFuture) "countdown to future event" else "time since past event"
        val formattedAge = age.format(person.displayUnits)

        val contextHint = when {
            description.contains("daughter", ignoreCase = true) ||
            description.contains("son", ignoreCase = true) ||
            description.contains("child", ignoreCase = true) ||
            description.contains("kid", ignoreCase = true) -> "child"

            description.contains("parent", ignoreCase = true) ||
            description.contains("mom", ignoreCase = true) ||
            description.contains("dad", ignoreCase = true) ||
            description.contains("mother", ignoreCase = true) ||
            description.contains("father", ignoreCase = true) ||
            description.contains("grandpa", ignoreCase = true) ||
            description.contains("grandma", ignoreCase = true) -> "parent/elder"

            description.contains("wedding", ignoreCase = true) ||
            description.contains("anniversary", ignoreCase = true) -> "celebration"

            description.contains("holiday", ignoreCase = true) ||
            description.contains("vacation", ignoreCase = true) ||
            description.contains("trip", ignoreCase = true) -> "holiday"

            else -> "general"
        }

        val toneGuidance = when (contextHint) {
            "child" -> "Emphasize cherishing moments, growth, and the fleeting nature of childhood."
            "parent/elder" -> "Express warmth, gratitude, and the importance of spending time together."
            "celebration" -> "Build excitement and anticipation for the special day."
            "holiday" -> "Create excitement and anticipation for the upcoming trip."
            else -> "Be warm and personal."
        }

        return """You are writing short widget messages for a time-tracking app called TimeKeeper.

Entry name: $name
Description: $description
Type: $type
Current value: $formattedAge

$toneGuidance

Generate exactly 5 short messages (max 50 characters each). Each message should:
- Be warm and personal
- Reference the name "$name" and the time
- Fit on a small widget
- Vary in tone (some excited, some reflective)

Format: Number each message 1-5, one per line.

Example format:
1. Emma turns 10 in just 2 weeks!
2. Cherish today with Emma
3. Only 14 days until Emma's birthday
4. 10 years of memories ahead
5. Time flies - enjoy every moment

Your messages:"""
    }

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

    private fun Long.formatCompact(): String {
        return when {
            this >= 1_000_000 -> "${this / 1_000_000}M"
            this >= 1_000 -> "${this / 1_000}K"
            else -> this.toString()
        }
    }
}
