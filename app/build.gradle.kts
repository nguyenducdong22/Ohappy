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

    // <<< VỊ TRÍ CHÍNH XÁC CỦA buildFeatures >>>
    buildFeatures {
        viewBinding = true // Đã bật View Binding cho AccountActivity
    }
    // <<< KẾT THÚC VỊ TRÍ CHÍNH XÁC >>>
}

dependencies {
    // Các thư viện AndroidX cơ bản
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.room.runtime.android)


    // Thư viện kiểm thử
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Thư viện bên thứ ba bạn đã thêm
    implementation(libs.circleimageview) // Circular ImageView
    implementation(libs.okhttp)          // OkHttp for networking
    implementation(libs.annotation)      // AndroidX Annotations (@NonNull)

    // <<< THƯ VIỆN CHO VIEWMODEL VÀ LIVEDATA (MỚI THÊM) >>>
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)
    // Nếu bạn muốn dùng các tiện ích mở rộng KTX (Java cũng có thể dùng)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // Annotation Processor cần thiết cho LiveData/ViewModel
    annotationProcessor(libs.androidx.lifecycle.compiler)
    implementation(libs.gson)
    // <<< KẾT THÚC THÊM >>>
    implementation(libs.core)
    implementation (libs.androidsvg) // Sử dụng phiên bản mới nhất



}