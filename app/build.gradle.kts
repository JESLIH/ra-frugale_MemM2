plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    kotlin("kapt")
}

android {
    namespace = "mg.ratra.agentique"
    compileSdk = 35

    defaultConfig {
        applicationId = "mg.ratra.agentique"
        minSdk = 26        // Camera2 pleinement stable + SensorManager complet
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-skeleton"

        // Config JNI : une seule bibliothèque native regroupant llama.cpp,
        // vosk et hnswlib pour éviter trois runtimes JNI séparés (cf. §1 architecture)
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                arguments += listOf(
                    "-DANDROID_STL=c++_shared",
                    "-DCMAKE_SHARED_LINKER_FLAGS=-Wl,-z,max-page-size=16384"
                )
            }
        }
        ndk {
            // Se limiter à arm64-v8a : c'est le jeu d'instructions du parc
            // ARM visé (§0.3 de la roadmap) ; armeabi-v7a peut être ajouté
            // seulement si le benchmark phase 1 exige de couvrir des
            // terminaux 32-bit plus anciens.
            abiFilters += "arm64-v8a"
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false // à activer une fois le pipeline stabilisé
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // --- Base Android / Kotlin ---
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // --- Injection de dépendances (isole domain/ d'Android, cf. §1) ---
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")

    // --- Stockage local (metadata assets, cf. §2) ---
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    // NB : l'index spatial du .pbf (pbf_index.db) est un SQLite brut avec
    // le module R-Tree, pas Room — Room gère uniquement les métadonnées
    // d'assets. Room court-circuiterait le SQL spatial nécessaire au R-Tree.

    // --- WebView locale pour le contenu riche (§1) ---
    implementation("androidx.webkit:webkit:1.11.0")
    // --- Reconnaissance vocale offline (étape 3) ---
    implementation("com.alphacephei:vosk-android:0.3.75")
    implementation("net.java.dev.jna:jna:5.18.1@aar")
    implementation("androidx.activity:activity-ktx:1.9.2")
    // --- Tests ---
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}
