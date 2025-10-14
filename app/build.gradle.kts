plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.zerowaste"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.zerowaste"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
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
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Core Android & UI Dependencies
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // --- MVVM Architecture Components ---
    // ViewModel: Manages UI-related data in a lifecycle-conscious way
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    // For using the 'by viewModels()' delegate
    implementation("androidx.activity:activity-ktx:1.9.0")


    // --- Networking (Retrofit & Gson) ---
    // Retrofit: A type-safe HTTP client for Android and Java
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Gson Converter: Converts JSON responses into Kotlin data classes
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // --- Coroutines for Asynchronous Programming ---
    // Android-specific extensions for Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // ADD THIS: For Icons.Extended (e.g., DeleteSweep)
    implementation("androidx.compose.material:material-icons-extended")

    // ADD THIS: For pull-to-refresh and ExperimentalMaterialApi
    // Note: Pull-to-refresh is currently in the M2 library, not M3
    implementation("androidx.compose.material:material:1.6.7")

    // ADD THIS: For the viewModel() composable function
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

        // --- Testing ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}