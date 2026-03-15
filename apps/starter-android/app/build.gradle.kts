plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.onlystack.starterapp"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.onlystack.starterapp"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:3000/api/v1\"")
            buildConfigField("String", "ENVIRONMENT", "\"development\"")
            buildConfigField("boolean", "RUNTIME_FIXTURE_MODE", "false")
            resValue("string", "app_name", "StarterApp (Dev)")
        }
        create("mock") {
            dimension = "environment"
            applicationIdSuffix = ".mock"
            versionNameSuffix = "-mock"
            buildConfigField("String", "API_BASE_URL", "\"https://fixture.invalid/api/v1\"")
            buildConfigField("String", "ENVIRONMENT", "\"mock\"")
            buildConfigField("boolean", "RUNTIME_FIXTURE_MODE", "true")
            resValue("string", "app_name", "StarterApp (Mock)")
        }
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            buildConfigField("String", "API_BASE_URL", "\"https://api-staging.yourapp.com/api/v1\"")
            buildConfigField("String", "ENVIRONMENT", "\"staging\"")
            buildConfigField("boolean", "RUNTIME_FIXTURE_MODE", "false")
            resValue("string", "app_name", "StarterApp (Staging)")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "API_BASE_URL", "\"https://api.yourapp.com/api/v1\"")
            buildConfigField("String", "ENVIRONMENT", "\"production\"")
            buildConfigField("boolean", "RUNTIME_FIXTURE_MODE", "false")
            resValue("string", "app_name", "StarterApp")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isDebuggable = true
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.adaptive)
    implementation(libs.activity.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.coroutines.android)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.google.android.play:review-ktx:2.0.1")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.biometric)
    implementation(libs.workmanager)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // Firebase — add google-services.json from Firebase Console, then apply plugins:
    //   'com.google.gms.google-services' + 'com.google.firebase.crashlytics'
    // val firebaseBom = platform("com.google.firebase:firebase-bom:33.0.0")
    // implementation(firebaseBom)
    // implementation("com.google.firebase:firebase-crashlytics-ktx")
    // implementation("com.google.firebase:firebase-analytics-ktx")
    // implementation("com.google.firebase:firebase-messaging-ktx")  // FCM push notifications

    // Google Play Billing — in-app purchases and subscriptions (StoreKit 2 equivalent on Android)
    implementation(libs.billing)
    testImplementation(libs.junit5.api)
    testImplementation(libs.junit5.params)
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
