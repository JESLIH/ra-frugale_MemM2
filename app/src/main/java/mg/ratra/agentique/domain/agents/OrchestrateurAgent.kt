package mg.ratra.agentique.domain.agents

/**
 * Agent Orchestrateur : pilote le SLM local + le RAG embarqué (index HNSW).
 *
 * Contrainte dure à respecter dans l'implémentation (étape 5 de la feuille
 * de route) : garder le prompt final sous ~300-400 jetons (cf. §4), via
 * - un prompt système statique mis en cache KV,
 * - 2-3 chunks RAG maximum, jamais un document entier,
 * - aucun historique conversationnel multi-tour (traitement stateless),
 * - chain-of-thought explicitement désactivé dans le prompt,
 * - sortie contrainte par grammaire GBNF (JSON), pas de texte libre à parser.
 *
 * Avant toute génération, vérifier le cache de contenu déjà généré pour cet
 * entityId (cf. §2, AssetStorageManager) — chaque cache-hit économise un
 * appel au SLM, dont le coût est dominant dans le budget de latence.
 */
interface OrchestrateurAgent {

    suspend fun handle(request: AgentMessage.OrchestratorRequest): AgentMessage.OrchestratorOutput
}
