package mg.ratra.agentique.domain.agents

/**
 * Agent Géomaticien : interroge l'index spatial du .pbf.
 *
 * IMPORTANT : aucune de ces fonctions n'invoque le SLM. C'est du SQL/R-Tree
 * pur (cf. §2 et §4 de l'architecture) — l'appel de fonctions déterministe
 * est préféré à la génération de code pour des raisons de stabilité
 * d'exécution sur un terminal utilisateur non contrôlé (Luo et al., cité
 * dans l'état de l'art).
 *
 * Implémentation prévue à l'étape 2 de la feuille de route, une fois
 * pbf_index.db construit.
 */
interface GeomaticienAgent {

    /** Résout la référence déictique ("ce monument") au plus proche du regard. */
    suspend fun findNearestEntity(
        lat: Double,
        lon: Double,
        headingDegrees: Float
    ): AgentMessage.GeoResult?

    suspend fun findEntitiesInRadius(
        lat: Double,
        lon: Double,
        radiusMeters: Int
    ): List<AgentMessage.GeoResult>

    suspend fun resolveToponym(name: String): AgentMessage.GeoResult?
}
