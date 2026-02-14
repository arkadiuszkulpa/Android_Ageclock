import com.github.triplet.gradle.androidpublisher.ReleaseStatus

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.play.publisher)
}

// Helper function to get property from environment or local.properties
fun getSecretProperty(name: String): String? {
    return System.getenv(name) ?: project.findProperty(name)?.toString()
}

android {
    namespace = "com.ageclock.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ageclock.app"
        minSdk = 26
        targetSdk = 35

        // Version management: use environment variables in CI, fallback to defaults locally
        val versionCodeValue = getSecretProperty("VERSION_CODE")?.toIntOrNull() ?: 1
        val versionNameValue = getSecretProperty("VERSION_NAME") ?: "1.0.0"

        versionCode = versionCodeValue
        versionName = versionNameValue

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystoreFilePath = getSecretProperty("KEYSTORE_FILE")
            if (keystoreFilePath != null && file(keystoreFilePath).exists()) {
                storeFile = file(keystoreFilePath)
                storePassword = getSecretProperty("KEYSTORE_PASSWORD")
                keyAlias = getSecretProperty("KEY_ALIAS")
                keyPassword = getSecretProperty("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Only use release signing if configured
            val releaseSigningConfig = signingConfigs.findByName("release")
            if (releaseSigningConfig?.storeFile != null) {
                signingConfig = releaseSigningConfig
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Play Store publishing configuration
play {
    val serviceAccountFile = getSecretProperty("PLAY_SERVICE_ACCOUNT_JSON")
    if (serviceAccountFile != null && file(serviceAccountFile).exists()) {
        serviceAccountCredentials.set(file(serviceAccountFile))
    }

    // Default to internal testing track
    track.set("internal")

    // Mark release as completed (not draft)
    releaseStatus.set(ReleaseStatus.COMPLETED)

    // Use AAB format (required for Play Store)
    defaultToAppBundles.set(true)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // ViewModel Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // On-device LLM inference (to be enabled when library integration is tested)
    // implementation(libs.kotlin.llamacpp)

    // DataStore for preferences
    implementation(libs.androidx.datastore.preferences)

    // JSON serialization
    implementation(libs.kotlinx.serialization.json)
}
