package mg.ratra.agentique.core.common

/**
 * Distance de Levenshtein — nombre minimal d'insertions/suppressions/
 * substitutions pour transformer [a] en [b]. Utilisée pour tolérer les
 * erreurs de reconnaissance vocale de Vosk sur les toponymes malgaches
 * (cf. limitation documentée : le modèle acoustique français n'a jamais
 * été entraîné sur les phonèmes malgaches).
 */
object Levenshtein {

    fun distance(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        val prev = IntArray(b.length + 1) { it }
        val curr = IntArray(b.length + 1)

        for (i in 1..a.length) {
            curr[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] = minOf(
                    curr[j - 1] + 1,      // insertion
                    prev[j] + 1,          // suppression
                    prev[j - 1] + cost    // substitution
                )
            }
            for (j in 0..b.length) prev[j] = curr[j]
        }
        return prev[b.length]
    }

    /**
     * Seuil de tolérance proportionnel à la longueur : un mot court tolère
     * peu d'erreurs, un mot long en tolère davantage.
     */
    fun maxToleratedDistance(length: Int): Int =
        (length * 0.3).toInt().coerceAtLeast(1).coerceAtMost(4)
}