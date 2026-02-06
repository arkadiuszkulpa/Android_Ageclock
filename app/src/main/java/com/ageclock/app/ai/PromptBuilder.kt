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
        val messages = mutableListOf<String>()

        if (age.isFuture) {
            // Countdown messages
            when {
                age.totalDays <= 7 -> {
                    messages.add("Only ${age.days} days until $name!")
                    messages.add("$name is almost here!")
                    messages.add("Just ${age.days} days to go!")
                    messages.add("The countdown is on for $name")
                    messages.add("Get ready - $name is coming soon!")
                }
                age.totalDays <= 30 -> {
                    messages.add("${age.days} days until $name")
                    messages.add("$name is just around the corner")
                    messages.add("Looking forward to $name")
                    messages.add("Not long now until $name!")
                    messages.add("Counting down to $name")
                }
                age.months > 0 -> {
                    messages.add("${age.months} months until $name")
                    messages.add("$name is coming in ${age.months} months")
                    messages.add("Mark your calendar for $name")
                    messages.add("Looking ahead to $name")
                    messages.add("The wait for $name continues")
                }
                else -> {
                    messages.add("${age.years} years until $name")
                    messages.add("$name is ${age.years} years away")
                    messages.add("Planning ahead for $name")
                }
            }
        } else {
            // Past date messages (age/anniversary)
            when {
                age.years == 0 && age.months < 12 -> {
                    messages.add("$name is ${age.months} months old")
                    messages.add("${age.months} months with $name")
                    messages.add("Cherish these early days with $name")
                    messages.add("$name - ${age.totalDays} precious days")
                    messages.add("Every day with $name is special")
                }
                age.years in 1..10 -> {
                    messages.add("$name is ${age.years} years old")
                    messages.add("${age.years} wonderful years with $name")
                    messages.add("Cherish every moment with $name")
                    messages.add("$name - ${age.years} years of memories")
                    messages.add("Time flies with $name")
                }
                else -> {
                    messages.add("$name - ${age.years} years")
                    messages.add("${age.years} years of $name")
                    messages.add("Celebrating ${age.years} years")
                    messages.add("$name: ${age.totalDays.formatCompact()} days")
                    messages.add("A journey of ${age.years} years")
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
