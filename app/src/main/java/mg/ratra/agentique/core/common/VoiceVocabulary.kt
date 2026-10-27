package mg.ratra.agentique.core.common

/**
 * Vocabulaire fixe des mots de commande de la grammaire vocale contrainte.
 * Partagé entre OfflineSttEngine (grammaire Vosk) et
 * RuleBasedVoiceIntentParser (isolation du toponyme dans la phrase), pour
 * éviter que les deux listes divergent.
 */
object VoiceVocabulary {
    val COMMAND_WORDS = setOf(
        "affiche", "montre", "raconte", "donne",
        "histoire", "détails", "informations",
        "format", "vidéo", "texte", "modèle", "trois", "d"
    )
}