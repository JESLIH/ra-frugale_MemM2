package mg.ratra.agentique.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import mg.ratra.agentique.data.voice.OfflineSttEngine
import mg.ratra.agentique.domain.agents.GeomaticienAgent
import mg.ratra.agentique.domain.voice.VoiceIntentParser
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var sttEngine: OfflineSttEngine

    @Inject
    lateinit var voiceIntentParser: VoiceIntentParser

    @Inject
    lateinit var geomaticienAgent: GeomaticienAgent

    private val requestAudioPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startSttTest() else Log.e("MainActivity", "Permission micro refusée")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startSttTest()
        } else {
            requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startSttTest() {
        lifecycleScope.launch {
            sttEngine.startListening()
                .catch { e -> Log.e("MainActivity", "Erreur du flux vocal", e) }
                .collect { hypothesis ->
                    Log.d("MainActivity", "Reconnu : $hypothesis")
                    val intent = voiceIntentParser.parse(hypothesis)
                    Log.d("MainActivity", "Intent extrait : $intent")

                    val location = intent.targetLocation
                    if (location != null) {
                        val result = geomaticienAgent.resolveToponym(location)
                        Log.d("MainActivity", "resolveToponym(\"$location\") -> $result")
                    } else {
                        Log.d("MainActivity", "Aucun lieu détecté dans l'intention, resolveToponym non appelé")
                    }
                }
        }
    }
}