plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
}

android {
    namespace = "com.websbaba.nitigrow"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.websbaba.nitigrow"
        minSdk = 26              // Android 8.0 — covers 95%+ of Indian devices
        targetSdk = 36
        // Driven from CI so every uploaded build has a unique, monotonic code
        // (Play rejects a reused versionCode). Falls back to 1 for local builds.
        versionCode = System.getenv("ANDROID_VERSION_CODE")?.toIntOrNull() ?: 1
        versionName = System.getenv("ANDROID_VERSION_NAME") ?: "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        // Firebase auto-init defaults — overridden per-build below.
        // Off everywhere until a real google-services.json is in place.
        manifestPlaceholders["firebaseCrashlyticsEnabled"] = "false"
        manifestPlaceholders["firebasePerfEnabled"] = "false"
        manifestPlaceholders["firebaseAnalyticsEnabled"] = "false"
        manifestPlaceholders["firebaseMessagingAutoInit"] = "false"

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
        }
    }

    signingConfigs {
        create("release") {
            // Pulled from ~/.gradle/gradle.properties or CI env — keystore never lives in git.
            val storeFilePath = (findProperty("NITIGROW_STORE_FILE") as String?)
                ?: System.getenv("NITIGROW_STORE_FILE")
            if (storeFilePath != null) {
                storeFile = file(storeFilePath)
                storePassword = (findProperty("NITIGROW_STORE_PASSWORD") as String?)
                    ?: System.getenv("NITIGROW_STORE_PASSWORD")
                keyAlias = (findProperty("NITIGROW_KEY_ALIAS") as String?)
                    ?: System.getenv("NITIGROW_KEY_ALIAS")
                keyPassword = (findProperty("NITIGROW_KEY_PASSWORD") as String?)
                    ?: System.getenv("NITIGROW_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            // No staging host deployed yet — point debug at the live API so dev/QA
            // exercises real data. TODO: switch to staging once it exists.
            buildConfigField("String", "BASE_URL", "\"https://api.nitigrow.in/api/\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
        }
        create("staging") {
            initWith(getByName("release"))
            isMinifyEnabled = true
            isShrinkResources = true
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            matchingFallbacks += listOf("release")
            buildConfigField("String", "BASE_URL", "\"https://staging.api.nitigrow.in/api/\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            signingConfig = signingConfigs.getByName("debug")
            // Real google-services.json is in place — enable Firebase for the pre-prod build.
            manifestPlaceholders["firebaseCrashlyticsEnabled"] = "true"
            manifestPlaceholders["firebasePerfEnabled"] = "true"
            manifestPlaceholders["firebaseAnalyticsEnabled"] = "true"
            manifestPlaceholders["firebaseMessagingAutoInit"] = "true"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"https://api.nitigrow.in/api/\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
            signingConfig = signingConfigs.getByName("release")
            // Real google-services.json is in place — turn Firebase on for production.
            manifestPlaceholders["firebaseCrashlyticsEnabled"] = "true"
            manifestPlaceholders["firebasePerfEnabled"] = "true"
            manifestPlaceholders["firebaseAnalyticsEnabled"] = "true"
            manifestPlaceholders["firebaseMessagingAutoInit"] = "true"
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Fail loudly if a release artifact is requested without a signing keystore,
// instead of silently emitting an unsigned APK/AAB that Play Store rejects.
gradle.taskGraph.whenReady {
    val buildingReleaseArtifact = allTasks.any { task ->
        (task.name.startsWith("assemble") || task.name.startsWith("bundle")) &&
            task.name.contains("Release") && !task.name.contains("Staging")
    }
    if (buildingReleaseArtifact && android.signingConfigs.getByName("release").storeFile == null) {
        throw GradleException(
            "Release build requested but no signing keystore is configured. " +
                "Set NITIGROW_STORE_FILE, NITIGROW_STORE_PASSWORD, NITIGROW_KEY_ALIAS and " +
                "NITIGROW_KEY_PASSWORD (gradle properties or env) before building a release."
        )
    }
}

dependencies {
    // Core AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.profileinstaller)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.ui.text.google.fonts)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Misc
    implementation(libs.coil.compose)
    implementation(libs.timber)

    // Paging
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)

    // WorkManager + Hilt integration
    implementation(libs.work.runtime)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // Glance (home-screen widgets)
    implementation(libs.glance.appwidget)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)


    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Unit test
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)

    // Instrumented test
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
