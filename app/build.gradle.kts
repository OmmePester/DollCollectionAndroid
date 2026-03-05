plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.dollcollectionandroid"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.dollcollectionandroid"
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // for solving weird image rotations
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // for free network calls to get city names in JSON format
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // for calculating the planets (Swiss Ephemeris)
//    implementation("org.swisseph:swisseph-java:2.01.00-1")



}