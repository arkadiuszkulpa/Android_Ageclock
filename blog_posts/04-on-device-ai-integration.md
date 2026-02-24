# Bringing AI On-Device — Personalised Messages Without the Cloud

**Date:** 6 - 7 February 2026
**Phase:** On-Device AI Integration
**Tags:** On-Device AI, LLM, Qwen2, Privacy, Kotlin Coroutines, GGUF

---

## The Vision

Time Keeper already showed you the age of the people you care about. But numbers alone don't always resonate. What if the app could also say something meaningful — a gentle reminder about the time you're sharing, personalised to each person?

The catch: this had to work **entirely on-device**. No cloud APIs, no data leaving the phone, no subscriptions. The privacy-first promise of the app was non-negotiable.

## What We Built

### AI Architecture (Feb 6)

The first step was designing a modular AI layer that could support both an immediate fallback system and a future on-device LLM:

- **`MessageGenerator`** — the orchestrator that decides how to generate messages. It first attempts LLM-based generation and falls back to template-based messages if the model isn't available.
- **`PromptBuilder`** — a template engine that generates contextually appropriate messages based on the person's description (child, parent, partner, etc.) and their age. This serves as both the fallback system and the prompt source for the LLM.
- **`ModelManager`** — handles the download, storage, and lifecycle of the Qwen2 0.5B GGUF model (~395MB) from Hugging Face. Includes download progress tracking, storage checks, and model deletion.

The architecture means the app works immediately with curated template messages, while the on-device LLM is an optional enhancement the user can choose to download.

### AI Message Lifecycle (Feb 7)

With the architecture in place, the next session focused on making AI messages a seamless part of the user experience:

- **Message generation and storage** — each person gets 5 personalised messages stored as JSON in the Room database, avoiding repeated inference.
- **Message rotation** — every time the user opens the app, the displayed message index increments, so they see a different message each time without triggering regeneration.
- **Proper regeneration logic** — messages are regenerated when the user changes display units or explicitly requests it, not on every app launch.
- **Widget integration** — AI messages display on the home screen widget, rotating alongside the live age data.
- **Calculation refactoring** — the age calculation logic was refactored to cleanly support the AI layer, ensuring the right data flows to the message generator.

## Skills Used

- **On-device ML architecture** — designing a system that gracefully degrades between LLM inference and template-based fallbacks.
- **GGUF model management** — working with quantised LLM models for mobile deployment, including download management from Hugging Face.
- **Kotlin Coroutines and Flow** — managing long-running model downloads and inference without blocking the UI thread.
- **Room database migrations** — adding new AI-related columns (`description`, `aiMessages`, `aiMessagesGeneratedAt`) with a proper migration from database version 1 to 2.
- **Context-aware prompt engineering** — building a template system that detects relationship types (child, parent, partner) from free-text descriptions and generates appropriate messages.
- **JSON serialisation** — using Kotlinx Serialization to store and retrieve structured message arrays within a single database column.

## What This Means for the User

Users get personalised, thoughtful messages about the people in their lives — and it all happens on their phone. There's no account to create, no API key to manage, no monthly fee. Users who want richer AI-generated messages can download the model; those who prefer simplicity still get curated messages that feel personal.

The message rotation means the widget stays fresh. Each time you glance at your home screen, you see a different reflection on the time you share with someone — without ever repeating the same message back-to-back.

## Technical Highlight: The Fallback Pattern

The fallback system deserves special mention. Rather than treating template messages as a lesser experience, they were designed to be genuinely useful on their own:

```
"Your daughter is 5 years old — savour these moments,
they grow up faster than you think."
```

The LLM enhances this with more variety and nuance, but the baseline is strong enough that many users may never feel the need to download the model. This pattern — design the fallback as a feature, not an afterthought — is broadly applicable to any AI-enhanced product.

## Reflection

Integrating AI into a mobile app without cloud dependencies is a meaningful technical challenge. The model size (395MB) requires careful UX around downloads and storage management. The inference speed on mobile hardware means you can't generate messages on-the-fly during scrolling — hence the pre-generation and caching strategy.

This phase demonstrated that privacy and AI aren't mutually exclusive. With the right architecture, you can offer intelligent features while keeping the user's data entirely under their control.

---

*This is Part 4 of the Time Keeper development series. Previous: [Rebrand to Time Keeper](03-rebrand-to-time-keeper.md) | Next: [Going Live on Google Play](05-going-live-on-google-play.md).*
