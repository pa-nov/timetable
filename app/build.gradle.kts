plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.panov.timetable"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.panov.timetable"
        minSdk = 26
        targetSdk = 36
        versionCode = 120
        versionName = "1.2.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.android.material)
}