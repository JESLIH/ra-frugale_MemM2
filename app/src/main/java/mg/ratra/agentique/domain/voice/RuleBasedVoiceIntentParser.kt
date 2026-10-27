package mg.ratra.agentique.domain.voice

import mg.ratra.agentique.core.common.VoiceVocabulary
import mg.ratra.agentique.domain.agents.OutputModality
import org.json.JSONObject
import javax.inject.Inject

class RuleBasedVoiceIntentParser @Inject constructor() : VoiceIntentParser {

    override fun parse(recognizedText: String): VoiceIntent {
        val text = extractText(recognizedText).lowercase()

        val modality = when {
            "vidéo" in text || "video" in text -> OutputModality.VIDEO
            "modèle" in text || "3d" in text -> OutputModality.MODEL_3D
            else -> OutputModality.TEXT
        }

        val explicitQuery = when {
            "histoire" in text -> "histoire"
            "détails" in text -> "détails"
            "informations" in text -> "informations"
            else -> null
        }

        // Tout mot qui n'est pas un mot de commande connu est considéré
        // comme faisant partie du toponyme demandé.
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        val locationWords = words.filterNot { it in VoiceVocabulary.COMMAND_WORDS }
        val targetLocation = locationWords.joinToString(" ").ifBlank { null }

        return VoiceIntent(
            targetLocation = targetLocation,
            modality = modality,
            explicitQuery = explicitQuery
        )
    }

    private fun extractText(raw: String): String = try {
        JSONObject(raw).optString("text", raw)
    } catch (e: Exception) {
        raw
    }
}