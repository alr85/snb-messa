plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // ✅ Compose compiler plugin (for Kotlin 2.x)
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0"

    // ✅ KSP plugin — must match your Kotlin version!
    id("com.google.devtools.ksp") version "2.2.20-2.0.4"
}



android {
    namespace = "com.example.mecca"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.mecca"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }


    buildFeatures {
        compose = true
    }


    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
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
    implementation(libs.androidx.navigation.compose)

    implementation (libs.squareup.okhttp3.logging.interceptor)

    // Room dependencies
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.runtime.annotation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3.window.size.class1)
    ksp(libs.androidx.room.compiler)              // ✅ use KSP for code generation
    implementation(libs.androidx.room.compiler)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.core.i18n)

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.converter.gson)
    implementation(libs.squareup.retrofit)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.icons.lucide)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidxComposeMaterial3)
    implementation(libs.androidxLifecycleRuntimeCompose)
    implementation(libs.kotlinxCoroutinesCore)
    implementation(libs.accompanist.flowlayout)

    configurations.all {
        exclude(group = "com.intellij", module = "annotations")
        exclude(group = "com.google.api.grpc", module = "proto-google-common-protos")
        exclude(group = "com.google.protobuf", module = "protobuf-java")
        exclude(group = "com.google.auto.value", module = "auto-value")
    }



}
