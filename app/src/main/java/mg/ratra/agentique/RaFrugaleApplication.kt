package mg.ratra.agentique

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Point d'entrée de l'application.
 *
 * C'est ici, et nulle part ailleurs, que les runtimes natifs seront chargés
 * (System.loadLibrary) et que le séquencement mémoire décrit en §6 de
 * l'architecture (ne jamais faire cohabiter SLM + décodage vidéo) sera piloté
 * via AgentCoordinator, une fois ce dernier implémenté (étape 4 de la feuille
 * de route).
 */
@HiltAndroidApp
class RaFrugaleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // TODO(étape 5) : System.loadLibrary("ra_native") une fois le module
        // JNI (llama.cpp + vosk + hnswlib) compilé — voir src/main/cpp/CMakeLists.txt
    }
}
