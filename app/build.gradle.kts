plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.elhajri.noor"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.elhajri.noor"
        minSdk = 24
        targetSdk = 34
        versionCode = 50
        versionName = "5.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // مفتاح Gemini يأتي من سرّ GitHub Actions (لا يُخزن في الكود أبداً لأن المستودع عام)