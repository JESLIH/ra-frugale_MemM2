package mg.ratra.agentique.domain.voice

import mg.ratra.agentique.domain.agents.OutputModality

/**
 * Résultat du parsing d'une commande vocale, ex. :
 * "Affiche-moi l'histoire de ce monument en format vidéo"
 *  -> targetLocation = null (référence déictique, résolue par le Géomaticien
 *     via GPS + boussole, pas ici)
 *  -> modality = VIDEO
 *  -> explicitQuery = "histoire"
 *
 * L'extraction se fait par règles simples sur les slots {action, modalité},
 * PAS par le SLM (cf. §3 de l'architecture) — ça ne consomme aucun jeton du
 * budget critique.
 */
data class VoiceIntent(
    val targetLocation: String?,
    val modality: OutputModality,
    val explicitQuery: String?
)

/**
 * Contrat du parseur d'intention. Implémentation prévue étape 3, une fois
 * la grammaire contrainte Vosk (fixedVocab + toponymes) en place.
 */
interface VoiceIntentParser {
    fun parse(recognizedText: String): VoiceIntent
}
