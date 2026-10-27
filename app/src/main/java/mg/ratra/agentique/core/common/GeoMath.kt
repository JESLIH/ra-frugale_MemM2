package mg.ratra.agentique.core.common

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GeoMath {
    private const val EARTH_RADIUS_M = 6_371_000.0

    /** Distance en mètres entre deux points GPS (formule de Haversine). */
    fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_M * c
    }

    /**
     * Delta approximatif en degrés de latitude/longitude pour un rayon donné
     * en mètres, utilisé comme pré-filtre bounding-box avant l'affinage par
     * Haversine (§2 : éviter de scanner toute la table à chaque requête).
     * Approximation suffisante à l'échelle d'un rayon de recherche de
     * quelques dizaines/centaines de mètres.
     */
    fun degreeDeltaForRadius(radiusMeters: Int, atLatitude: Double): Pair<Double, Double> {
        val latDelta = radiusMeters / 111_320.0
        val lonDelta = radiusMeters / (111_320.0 * cos(Math.toRadians(atLatitude)).coerceAtLeast(0.01))
        return latDelta to lonDelta
    }
}
