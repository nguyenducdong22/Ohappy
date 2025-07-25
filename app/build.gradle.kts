plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.noname"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.noname"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // === THAY ĐỔI CÁC DÒNG NÀY CHO KOTLIN DSL ===
        renderscriptTargetApi = 18 // Dấu bằng thay vì khoảng trắng
        renderscriptSupportModeEnabled = true // Dấu bằng thay vì khoảng trắng
        // ===========================================
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.circleimageview)
    implementation(libs.constraintlayout.v214)
}
android {
    // ... các cấu hình khác
    buildFeatures {
        viewBinding = true
    }
}
