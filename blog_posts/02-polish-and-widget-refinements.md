# Squashing Bugs and Refining the Widget Experience

**Date:** 1 - 2 February 2026
**Phase:** Polish and Refinement
**Tags:** Android Widgets, Bug Fixing, UX, WorkManager

---

## Moving from "It Works" to "It Works Well"

With the core functionality in place, this phase was about turning a working prototype into something that felt polished and reliable. The focus shifted to edge cases, widget behaviour, and the small details that make the difference between a hobby project and something you'd actually want on your home screen every day.

## What We Tackled

### Bug Fix: Multiple Entries Breaking the Timer

The first issue discovered was that adding more than one person caused the time counter to behave unexpectedly. The root cause was in how coroutine flows were scoped — each person's live age ticker needed its own independent flow, properly lifecycle-aware so it wouldn't leak or interfere with others. This was resolved by ensuring each `CalculatedAge` flow was correctly scoped within the ViewModel's coroutine context.

### Widget Text Wrapping

The home screen widget had a practical problem: longer names or age strings would get clipped rather than wrapping to a new line. This was a layout constraint issue within the widget's `RemoteViews`. Adjusting the text view properties to support multi-line rendering ensured that even users with longer names or full time-unit breakdowns could read everything at a glance.

### Widget Refresh on App Resume

A subtle but important UX issue: when a user opened the app and then returned to the home screen, the widget would still show stale data until the next scheduled `WorkManager` update (up to 15 minutes later). The fix was to trigger a widget data refresh in the Activity's `onResume` lifecycle callback, so the widget always reflects the latest state when the user returns to their home screen.

## Skills Used

- **Debugging coroutine flows** — understanding how Kotlin `Flow` instances interact when multiple are collected simultaneously within a shared scope.
- **Android Widget layout constraints** — working within the limited `RemoteViews` API, which doesn't support the full flexibility of Compose or standard Android views.
- **Activity lifecycle management** — using `onResume` to bridge the gap between the app's internal state and the widget's display.
- **WorkManager scheduling** — understanding the 15-minute minimum interval constraint and designing around it with immediate update triggers.

## What This Means for the User

These changes are the kind that users don't notice directly — they just expect things to work. The widget shows the right data when they glance at their phone. Names don't get cut off. Adding multiple people doesn't cause glitches. This is the invisible work that separates a reliable app from a frustrating one.

## Reflection

This phase reinforced an important principle: the first 80% of a feature gets it working, but the remaining 20% of polish is what makes it trustworthy. Widget development on Android in particular demands this attention — users see widgets constantly, so any visual or data inconsistency is immediately noticeable.

---

*This is Part 2 of the Time Keeper development series. Previous: [From Idea to Working App](01-from-idea-to-working-app.md) | Next: [Rebrand to Time Keeper](03-rebrand-to-time-keeper.md).*
