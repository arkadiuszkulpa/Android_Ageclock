# From Idea to Working Android App in a Day

**Date:** 31 January 2026
**Phase:** Project Kickoff
**Tags:** Android, Kotlin, Jetpack Compose, Rapid Prototyping

---

## The Idea

Everyone marks birthdays on the calendar, but what if you could see the age of the people who matter most — live, updating every second? That was the spark behind what would become **Time Keeper**: a real-time age tracker for Android.

The goal was straightforward — build an app that takes a date of birth and displays a continuously ticking age. But the execution needed to be modern, performant, and built on a solid architectural foundation from day one.

## What We Built

In a single development session, the project went from an empty directory to a fully functional Android application:

- **A basic clock application** scaffolded with Kotlin and Jetpack Compose, establishing the project structure, Gradle configuration, and Material Design 3 theming.
- **Real-time age calculation** — the core feature. Given a date of birth, the app computes a live breakdown of years, months, days, hours, minutes, and seconds, updating every second using Kotlin Coroutines and Compose state management.
- **A home screen widget** so users can see ages at a glance without opening the app, built with `AppWidgetProvider` and `WorkManager` for reliable background updates.
- **Customisable display granularity** — users can choose which time units to show (just years and months, or the full breakdown down to seconds). This is implemented via a bitmask system that keeps the data model compact.
- **Countdown mode** — adding a future date automatically switches from "age" to "countdown", making the app useful for events like weddings, due dates, or milestones.

## Skills Used

- **Kotlin** — the primary language, leveraging coroutines for asynchronous operations and Flow for reactive data streams.
- **Jetpack Compose** — Android's modern declarative UI toolkit, used for the entire interface. State management via `mutableStateOf` and `LaunchedEffect` for side effects like the ticking timer.
- **Material Design 3** — the latest design system from Google, providing a clean, accessible UI out of the box.
- **Room Database** — local SQLite persistence with type-safe queries, handling the storage of people and their birth dates.
- **Android App Widgets** — extending the app's reach to the home screen with live-updating widgets.
- **WorkManager** — scheduling reliable background tasks for widget updates.
- **MVVM Architecture** — separating concerns cleanly with ViewModels, StateFlow, and a Repository pattern from the start.

## What This Means for the User

From day one, Time Keeper was a usable product. A user could install the app, add the people they care about, customise how age is displayed, and pin a widget to their home screen — all with zero accounts, zero cloud dependencies, and zero data collection. The app runs entirely on-device, which was a deliberate architectural choice made at the outset.

## Challenges and Fixes

The first session wasn't without its hiccups. A compatibility issue with Material 3's `selectableDates` API (from an outdated library version) caused a build failure, which was diagnosed and resolved by updating to the correct Compose BOM version. This was an early lesson in the importance of version management in the Android ecosystem — something that would become a recurring theme.

## Looking Back

Starting with a solid foundation — Kotlin, Compose, Room, MVVM — paid dividends immediately. Every feature added later built cleanly on top of this architecture rather than fighting against it. The decision to go privacy-first from day one also meant there was no technical debt around analytics or cloud services to untangle later.

---

*This is Part 1 of the Time Keeper development series. Next: [Polish and Widget Refinements](02-polish-and-widget-refinements.md).*
