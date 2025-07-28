plugins {
    alias(libs.plugins.android.application)
    // Nếu bạn có plugin Kotlin (như org.jetbrains.kotlin.android), hãy giữ nó
    // alias(libs.plugins.kotlin.android)
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

        renderscriptTargetApi = 18
        renderscriptSupportModeEnabled = true
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
        // Đảm bảo Java 8 (hoặc cao hơn) để hỗ trợ các tính năng LiveData/ViewModel
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
}