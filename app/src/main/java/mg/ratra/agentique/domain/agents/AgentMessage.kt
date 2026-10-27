package mg.ratra.agentique.domain.agents

import kotlinx.serialization.Serializable

/**
 * Protocole d'échange strict entre l'agent Géomaticien et l'agent Orchestrateur.
 *
 * Principe directeur (cf. §4 de l'architecture) : ces messages sont des
 * données structurées et minimales, JAMAIS du texte libre régénéré par un
 * modèle de langage intermédiaire. Chaque champ ici a un coût connu en
 * jetons une fois sérialisé pour le prompt de l'Orchestrateur — c'est ce qui
 * garde le budget N < 300-400 jetons/tour discuté en §4.
 *
 * Ce fichier ne contient volontairement aucune logique — seulement le
 * contrat de données. L'implémentation des agents vient à l'étape 4 de la
 * feuille de route.
 */
sealed class AgentMessage {

    @Serializable
    data class GeoQuery(
        val userLat: Double,
        val userLon: Double,
        val headingDegrees: Float
    ) : AgentMessage()

    @Serializable
    data class GeoResult(
        val entityId: Long,
        val name: String,
        val tags: Map<String, String>,
        val distanceMeters: Float
    ) : AgentMessage()

    @Serializable
    data class OrchestratorRequest(
        val geoResult: GeoResult,
        val modality: OutputModality,
        val explicitQuery: String?,
        val ragContext: List<String> // 2-3 chunks max — voir contrainte §4
    ) : AgentMessage()

    @Serializable
    data class OrchestratorOutput(
        val modality: OutputModality,
        val payload: String
    ) : AgentMessage()
}

@Serializable
enum class OutputModality { TEXT, VIDEO, MODEL_3D }
