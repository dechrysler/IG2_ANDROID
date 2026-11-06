plugins {
    alias(libs.plugins.android.application)
}

// URL base de la API. Se puede sobrescribir sin tocar código:
//   .\gradlew.bat assembleDebug -PAPI_BASE_URL=http://192.168.1.50:8080/api
// Por defecto apunta al equipo anfitrión desde el emulador Android.
val apiBaseUrl = (project.findProperty("API_BASE_URL") as String?)
    ?: "http://10.0.2.2:8080/api"

android {
    namespace = "com.example.sabordelhimalaya"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.sabordelhimalaya"
        minSdk = 31
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.glide)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
