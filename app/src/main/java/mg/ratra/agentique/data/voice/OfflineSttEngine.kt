package mg.ratra.agentique.data.voice

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONArray
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import javax.inject.Inject
import javax.inject.Singleton
import mg.ratra.agentique.core.common.VoiceVocabulary

private const val TAG = "OfflineSttEngine"
private const val MODEL_ASSET_FOLDER = "vosk-model-small-fr-0.22"
private const val SAMPLE_RATE = 16000.0f



private val FIXED_VOCAB = VoiceVocabulary.COMMAND_WORDS.toList()

@Singleton
class OfflineSttEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    init {
        LibVosk.setLogLevel(LogLevel.WARNINGS) // Masque les logs INFO (les 15 000 'Ignoring word') pour éviter de faire crasher logd
    }

    private var model: Model? = null
    private var speechService: SpeechService? = null

    fun startListening(): Flow<String> = callbackFlow {
        StorageService.unpack(
            context,
            MODEL_ASSET_FOLDER,
            "model",
            { unpackedModel ->
                model = unpackedModel
                try {
                    val recognizer = Recognizer(unpackedModel, SAMPLE_RATE, buildGrammar())
                    val service = SpeechService(recognizer, SAMPLE_RATE)
                    speechService = service
                    service.startListening(object : RecognitionListener {
                        override fun onResult(hypothesis: String?) {
                            hypothesis?.let { trySend(it) }
                        }
                        override fun onFinalResult(hypothesis: String?) {
                            hypothesis?.let { trySend(it) }
                        }
                        override fun onPartialResult(hypothesis: String?) {
                        }
                        override fun onError(exception: Exception?) {
                            Log.e(TAG, "Erreur de reconnaissance", exception)
                        }
                        override fun onTimeout() {
                            Log.w(TAG, "Timeout du reconnaisseur vocal")
                        }
                    })
                } catch (e: Exception) {
                    Log.e(TAG, "Échec de création du Recognizer", e)
                    close(e)
                }
            },
            { exception ->
                Log.e(TAG, "Échec du chargement du modèle Vosk", exception)
                close(exception)
            }
        )
        awaitClose {
            speechService?.stop()
            speechService?.shutdown()
        }
    }

    private fun buildGrammar(): String {
        val toponyms = loadToponyms()
        val all = FIXED_VOCAB + toponyms + listOf("[unk]")
        return JSONArray(all).toString()
    }

    private fun loadToponyms(): List<String> = try {
        context.assets.open("malagasy_toponyms.json").bufferedReader().use { reader ->
            val jsonArray = JSONArray(reader.readText())
            buildList { for (i in 0 until jsonArray.length()) add(jsonArray.getString(i)) }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Impossible de charger malagasy_toponyms.json", e)
        emptyList()
    }
}