// Pont JNI — squelette.
// Les fonctions natives exposées à LlmEngine.kt, OfflineSttEngine.kt et
// HnswIndexStore.kt (côté Kotlin) seront implémentées ici à l'étape 5.
//
// Exemple de signature à venir (illustratif, pas encore fonctionnel) :
//
// extern "C" JNIEXPORT jstring JNICALL
// Java_mg_ratra_agentique_data_llm_LlmEngine_nativeGenerate(
//     JNIEnv* env, jobject /* this */, jstring prompt) { ... }

#include <jni.h>

extern "C" JNIEXPORT jstring JNICALL
Java_mg_ratra_agentique_data_llm_LlmEngine_nativePing(
    JNIEnv* env, jobject /* this */) {
    return env->NewStringUTF("ra_native: squelette chargé, pas encore de LLM lié");
}
