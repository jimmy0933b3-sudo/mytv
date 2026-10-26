import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val gitCommitSha: String = try {
    val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD").start()
    process.inputStream.bufferedReader().readText().trim().ifEmpty { "unknown" }
} catch (e: Exception) {
    "unknown"
}

// CI can supply a monotonically increasing versionCode for installable updates.
val ciVersionCode = providers.gradleProperty("ciVersionCode").orNull?.toIntOrNull()

// Release signing is configured only when all four values are available.
// CI restores the keystore from Actions Secrets to a runner-temporary location.
val ciSigningStoreFile = providers.environmentVariable("ANDROID_KEYSTORE_PATH").orNull
val ciSigningStorePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").orNull
val ciSigningKeyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS").orNull
val ciSigningKeyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD").orNull

val buildTime: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").apply {
    timeZone = TimeZone.getTimeZone("UTC")
}.format(Date())

android {
    namespace = "com.taiwanfreetv.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.taiwanfreetv.app"
        minSdk = 24
        targetSdk = 34
        versionCode = ciVersionCode ?: 6
        versionName = "1.0.5"

        buildConfigField("String", "GIT_COMMIT_SHA", "\"$gitCommitSha\"")
        buildConfigField("String", "BUILD_TIME", "\"$buildTime\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        if (listOf(
                ciSigningStoreFile,
                ciSigningStorePassword,
                ciSigningKeyAlias,
                ciSigningKeyPassword
            ).all { !it.isNullOrBlank() }
        ) {
            create("ciRelease") {
                storeFile = file(requireNotNull(ciSigningStoreFile))
                storePassword = ciSigningStorePassword
                keyAlias = ciSigningKeyAlias
                keyPassword = ciSigningKeyPassword
            }
        }
    }
    buildTypes {
        release {
            // No debug-key fallback: CI requires a properly signed Release APK.
            signingConfigs.findByName("ciRelease")?.let { signingConfig = it }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INDEX"
        }
    }
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

    // Media3
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.ui)

    // DataStore & JSON
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.gson)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.ui.tooling)
}
