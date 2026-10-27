// Fichier de build racine : ne fait que déclarer les versions de plugins,
// aucune dépendance de module ici (voir app/build.gradle.kts).
plugins {
    id("com.android.application") version "8.6.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.24" apply false
}
