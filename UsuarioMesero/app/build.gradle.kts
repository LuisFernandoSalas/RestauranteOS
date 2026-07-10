plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.usuariomesero"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.usuariomesero"
        minSdk = 28
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

    // Room
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Gson
    implementation(libs.gson)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Retrofit (Para hacer peticiones HTTP a Laravel)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // Gson Converter (Para traducir el JSON a tus Modelos automáticamente)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp Logging (Opcional pero nivel Senior: Para ver los JSON crudos en el Logcat)
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
}