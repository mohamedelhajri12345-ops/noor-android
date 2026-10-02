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
        versionCode = 52
        versionName = "5.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

    }


    signingConfigs {
        create("noor") {
            // مفتاح توقيع ثابت وموحّد لكل الإصدارات — يُولَّد مرة واحدة فقط
            // في CI عبر خطوة "Ensure signing keystore" ثم يُحفظ في المستودع.
            // هذا يمنع خطأ "فشل التثبيت" الذي يحدث عندما يختلف توقيع كل نسخة.
            val ksFile = file("noor-release.keystore")
            if (ksFile.exists()) {
                storeFile = ksFile
                storePassword = "NoorSecure2026"
                keyAlias = "noorkey"
                keyPassword = "NoorSecure2026"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("noor")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // بناء التصحيح يستعمل نفس المفتاح الثابت أيضاً — كي تبقى كل الإصدارات
            // (تصحيح أو إصدار) متوافقة التوقيع فيما بينها ويمكن تحديثها فوق بعضها.
            signingConfig = signingConfigs.getByName("noor")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // تقسيم معالجات الهاتف: بدل حزم 4 معمارية معاً (126MB) نحزم arm64-v8a فقط
    // (كل الهواتف الحديثة 2018+) فيصير حجم الـAPK أقل من 70MB، مع إبقاء AAB كاملاً لـ Play.
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a")            // الهواتف الحديثة — يغطي 99% من الأجهزة
            isUniversalApk = false           // نُلغي الحزمة الشاملة الثقيلة
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.02.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.media:media:1.7.0")
    // الذكاء الاصطناعي المحلي: نموذج Gemma يعمل على هاتف المستخدم (MediaPipe GenAI)
    implementation("com.google.mediapipe:tasks-genai:0.10.35")
    // إعلانات AdMob الرسمية — تُستعمل معرّفات الاختبار حتى يزوّد المالك معرّفاته الحقيقية
    implementation("com.google.android.gms:play-services-ads:23.6.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
