plugins {
    alias(libs.plugins.android.application)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
}
apply {
    from("dimens.gradle.kts")
}

tasks.named("preBuild") {
    dependsOn("createSDP", "createSSP")
}

android {
    namespace = "com.example.facemash"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.facemash"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    buildFeatures{
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("com.airbnb.android:lottie:6.4.0")
    implementation("com.tbuonomo:dotsindicator:4.3")

    implementation("com.google.android.gms:play-services-mlkit-face-detection:17.1.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.13.1")

    implementation("androidx.media3:media3-exoplayer:1.1.0")
    implementation("androidx.media3:media3-ui:1.1.0")
    implementation("androidx.media3:media3-exoplayer-hls:1.1.0")

    implementation("androidx.camera:camera-core:1.4.2")
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")

    implementation("com.google.android.play:review-ktx:2.0.2")

    implementation("androidx.room:room-paging:2.6.1")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("com.google.dagger:hilt-android:2.57.1")
    implementation(libs.androidx.lifecycle.livedata.core.ktx)
    implementation(libs.androidx.recyclerview)
    ksp("com.google.dagger:hilt-android-compiler:2.57.1")

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}