# Google Play Store CI/CD Setup Guide

## Overview

This guide walks through setting up automated deployments to Google Play Store from GitHub Actions.

---

## 1. Keystore Setup

### Generate Release Keystore

```powershell
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -genkey -v -keystore ageclock-release.keystore -alias ageclock -keyalg RSA -keysize 2048 -validity 10000
```

You'll be prompted for:
- Keystore password (remember this!)
- Key password (can be same as keystore password)
- Your name, organization, city, etc. (can be anything)

### View Keystore Details

```powershell
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -list -v -keystore ageclock-release.keystore
```

### Find Keystore File

```powershell
Get-ChildItem -Path C:\Users -Recurse -Filter "*.keystore" -ErrorAction SilentlyContinue | Select-Object FullName
```

### Convert Keystore to Base64 (for GitHub Secret)

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\path\to\ageclock-release.keystore")) | Set-Clipboard
```

This copies the base64 string to your clipboard.

---

## 2. GitHub Secrets

Go to: **GitHub Repo → Settings → Secrets and variables → Actions → New repository secret**

Add these 5 secrets:

| Secret Name | Value |
|-------------|-------|
| `KEYSTORE_BASE64` | Base64-encoded keystore (from command above) |
| `KEYSTORE_PASSWORD` | Your keystore password |
| `KEY_ALIAS` | `ageclock` |
| `KEY_PASSWORD` | Your key password |
| `PLAY_SERVICE_ACCOUNT_JSON` | Full JSON content from Google Cloud (see below) |

---

## 3. Google Cloud Service Account

### Step 1: Create Project & Enable API

1. Go to [console.cloud.google.com](https://console.cloud.google.com)
2. Create a new project or select existing
3. Search for **"Google Play Android Developer API"**
4. Click **Enable**

### Step 2: Create Service Account

1. Go to **IAM & Admin → Service Accounts**
2. Click **Create Service Account**
3. Name: `play-publisher` (or any name)
4. Click **Create and Continue**
5. Skip roles (click **Continue**)
6. Click **Done**

### Step 3: Generate JSON Key

1. Click on the service account you created
2. Go to **Keys** tab
3. Click **Add Key → Create new key**
4. Select **JSON**
5. Click **Create** (downloads the file)

### Step 4: Link to Play Console

1. Go to [play.google.com/console](https://play.google.com/console)
2. Click **Settings** (gear icon) → **API access**
3. Click **Link** next to your Google Cloud project
4. Find your service account in the list
5. Click **Grant access**
6. Select permissions:
   - **App access**: Select your app (or all apps)
   - **Account permissions**: Check "Manage releases" and "View app information"
7. Click **Invite user**

### Step 5: Add JSON to GitHub

Open the downloaded JSON file, copy the entire contents, and paste as `PLAY_SERVICE_ACCOUNT_JSON` secret.

---

## 4. First Manual Upload (Required)

The Play Store API requires at least one manual upload before automated publishing works.

### Build Release AAB Locally

```powershell
# From project root
./gradlew bundleRelease
```

The AAB file will be at: `app/build/outputs/bundle/release/app-release.aab`

### Upload to Play Console

1. Go to [play.google.com/console](https://play.google.com/console)
2. Select your app (or create new app)
3. Go to **Release → Testing → Internal testing**
4. Click **Create new release**
5. Upload the AAB file
6. Add release notes
7. Click **Review and roll out**

### Complete Required Sections

Before you can release, complete these in Play Console:
- [ ] App content (privacy policy, ads, content rating)
- [ ] Store listing (description, screenshots, icon)
- [ ] Data safety section

---

## 5. How CI/CD Works

### On Pull Request / Push to dev
- Runs lint checks
- Runs unit tests
- Builds debug APK

### On Merge to main
- Runs lint and tests
- Builds signed release AAB
- Uploads to Play Store **internal testing** track

### Manual Production Release

Option 1: Trigger workflow manually
1. Go to **Actions → Publish to Play Store**
2. Click **Run workflow**
3. Select track: `production`

Option 2: Create a git tag
```bash
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
```

---

## 6. Verify Secrets Are Configured

### Check GitHub Secrets
Go to: **GitHub Repo → Settings → Secrets and variables → Actions**

You should see these 5 secrets listed:
- `KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`
- `PLAY_SERVICE_ACCOUNT_JSON`

### Test Keystore Locally

```powershell
# Set environment variables temporarily
$env:KEYSTORE_FILE = "C:\path\to\ageclock-release.keystore"
$env:KEYSTORE_PASSWORD = "your-password"
$env:KEY_ALIAS = "ageclock"
$env:KEY_PASSWORD = "your-key-password"

# Try building release
./gradlew bundleRelease
```

If successful, the signed AAB will be at `app/build/outputs/bundle/release/`.

---

## 7. Troubleshooting

### "No key with alias found"
- Check `KEY_ALIAS` matches what you used in keytool (run `-list -v` to verify)

### "Keystore was tampered with"
- Wrong password - regenerate the keystore

### "API access not enabled"
- Enable "Google Play Android Developer API" in Google Cloud Console
- Wait a few minutes for it to propagate

### "Permission denied" from Play Console
- Service account needs "Manage releases" permission
- Check API access settings in Play Console

### "App not found"
- Create the app in Play Console first
- Upload at least one AAB manually before using API

---

## Quick Reference

```powershell
# View keystore info
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -list -v -keystore ageclock-release.keystore

# Build debug APK
./gradlew assembleDebug

# Build release AAB (needs signing config)
./gradlew bundleRelease

# Run tests
./gradlew testDebugUnitTest

# Run lint
./gradlew lintDebug
```
