plugins {
    alias(libs.plugins.android.application)
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
    viewBinding {
        enable = false
    }
    // 新增：确保资源链接时识别Material组件（可选，但避免兼容问题）
    buildFeatures {
        compose = false // 关闭Compose，避免和ViewBinding冲突
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

    // Room依赖（Java项目用annotationProcessor，配置正确）
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // Lifecycle依赖（版本稳定，配置正确）
    val lifecycleVersion = "2.5.1"
    implementation("androidx.lifecycle:lifecycle-viewmodel:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

    // ========== 核心补充：RecyclerView依赖（之前缺失！） ==========
    val recyclerViewVersion = "1.3.2"
    implementation("androidx.recyclerview:recyclerview:$recyclerViewVersion")

    // 网络请求相关依赖
    val retrofitVersion = "2.9.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // 协程支持
    val coroutinesVersion = "1.6.4"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // 工具库
    implementation("com.google.code.gson:gson:2.9.0")

} 