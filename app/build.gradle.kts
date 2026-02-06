plugins {
    alias(libs.plugins.android.application)
    // 移除：id("kotlin-kapt")
}

android {
    namespace = "com.example.englishwordnotebook"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.englishwordnotebook"
        minSdk = 26
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
    // 保留ViewBinding
    viewBinding {
        enable = true
    }
}

dependencies {
    // 原有依赖保留
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Room依赖：将kapt改为annotationProcessor（Java项目用这个）
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // ========== 关键修改点 ==========
    // 1. 降低ViewModel版本（2.6.2更稳定，易下载）
    // 2. 只保留核心依赖，避免重复/冗余
    val lifecycleVersion = "2.5.1" // 2.5.1是更稳定的版本，几乎所有仓库都有
    implementation("androidx.lifecycle:lifecycle-viewmodel:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime:$lifecycleVersion")
}

// 移除：kapt { ... } 整个代码块