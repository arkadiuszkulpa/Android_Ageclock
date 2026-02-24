# Going Live — From Local Build to Google Play Store

**Date:** 14 February 2026
**Phase:** Deployment and Launch
**Tags:** CI/CD, GitHub Actions, Google Play Store, Privacy Policy, Gradle, Release Management

---

## The Final Push

Two weeks after the first line of code, Time Keeper was ready to meet real users. This phase was about everything that happens between "it works on my machine" and "it's available on the Play Store" — and there's more to it than you might think.

## What We Did

### CI/CD Pipeline

A full GitHub Actions pipeline was set up with two workflows:

- **`ci.yml`** — triggers on every pull request and push to the `dev` branch. Runs lint checks, unit tests, and builds a debug APK. This is the quality gate — nothing reaches `main` without passing.
- **`publish.yml`** — triggers on merge to `main` or via manual dispatch. Builds a signed release AAB (Android App Bundle), and publishes directly to the Google Play Store's internal testing track. Manual dispatch also allows publishing to alpha, beta, or production tracks.

The pipeline handles:
- Decoding a base64-encoded keystore from GitHub Secrets
- Signing the release build with the correct credentials
- Versioning via environment variables (with sensible defaults for local builds)
- Uploading to Google Play via the Play Publisher Gradle plugin

### Privacy Policy and Web Presence

Google Play requires a privacy policy URL. Since Time Keeper collects no user data, the policy is straightforward — but it still needs to exist and be publicly accessible:

- **`docs/privacy-policy.html`** — a clear, honest privacy policy hosted via GitHub Pages.
- **`docs/index.html`** — a simple landing page for the project's web presence.
- **GitHub Pages configuration** — ensuring the docs are served correctly from the repository.

### App Icon and Final Polish

- A new app icon was designed and integrated, replacing the default placeholder.
- Final lint issues were resolved to ensure the codebase was clean for release.
- The Gradle wrapper was committed to the repository, ensuring reproducible builds regardless of the developer's local Gradle installation.

### Google Play Store Setup

Publishing to the Play Store involves a non-trivial amount of configuration:

- **Keystore generation** — creating a release signing key with a 10,000-day validity.
- **Google Cloud service account** — setting up API access for automated publishing.
- **Play Console configuration** — linking the service account, setting permissions, completing the required store listing sections (content rating, data safety, app description).
- **First manual upload** — the Play Store API requires at least one manual AAB upload before automated publishing works.

All of this was documented in `PLAY_STORE_SETUP.md` as a step-by-step guide for future reference.

## Skills Used

- **GitHub Actions** — writing multi-step CI/CD workflows with conditional triggers, secrets management, and artefact handling.
- **Gradle build system** — configuring release signing, version management, and the Play Publisher plugin for automated deployments.
- **Google Play Console** — navigating the full app submission process: store listings, content ratings, data safety declarations, and release management.
- **Google Cloud Platform** — creating service accounts and configuring API access for the Play Android Developer API.
- **Secrets management** — storing sensitive credentials (keystore, passwords, service account JSON) as GitHub Secrets and consuming them safely in CI.
- **GitHub Pages** — hosting a privacy policy and landing page directly from the repository.
- **Release engineering** — understanding the full chain from source code to a signed, published app bundle on a distribution platform.

## What This Means for the User

Time Keeper is now available for download. Real users can install it, track the ages of the people they care about, and pin widgets to their home screens. Every future update follows a predictable path: code change, pull request, automated testing, merge, and automatic deployment to the Play Store.

The privacy policy gives users confidence that their data stays on their device. The CI/CD pipeline ensures that every release is built from tested, linted code — no "it works on my machine" releases.

## Reflection

The gap between "app is done" and "app is published" is wider than most developers expect. The Play Store has legitimate requirements around privacy, content ratings, and data safety that take real effort to address properly. Automated deployment is a significant upfront investment, but it pays for itself immediately — every subsequent release is a single merge to `main`.

Committing the Gradle wrapper might seem minor, but it's one of those details that matters for reproducibility. Anyone cloning this repository — or any CI runner — can build the project without worrying about which Gradle version is installed locally.

This phase also highlighted the value of documentation. The `PLAY_STORE_SETUP.md` guide captures every step of a process that involves three different platforms (GitHub, Google Cloud, Play Console) and five different secrets. Without that documentation, repeating the process for a new app would mean rediscovering each step from scratch.

---

*This is Part 5 of the Time Keeper development series. Previous: [On-Device AI Integration](04-on-device-ai-integration.md).*

---

## Series Summary

Over the course of two weeks, Time Keeper went from an idea to a published Android application:

| Date | Milestone |
|------|-----------|
| 31 Jan | Project kickoff — core app, widgets, and age calculation |
| 1-2 Feb | Bug fixes, widget polish, and UX refinements |
| 4-5 Feb | Rebrand from AgeClock to Time Keeper |
| 6-7 Feb | On-device AI integration with Qwen2 0.5B |
| 14 Feb | CI/CD pipeline, privacy policy, and Google Play launch |

The tech stack — Kotlin, Jetpack Compose, Room, WorkManager, and on-device AI — represents a modern, production-grade Android application built with privacy as a core principle rather than an afterthought.
