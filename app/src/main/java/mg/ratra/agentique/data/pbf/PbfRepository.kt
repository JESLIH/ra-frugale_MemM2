package mg.ratra.agentique.data.pbf

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mg.ratra.agentique.core.common.GeoMath
import mg.ratra.agentique.core.common.Levenshtein
import mg.ratra.agentique.domain.agents.AgentMessage
import mg.ratra.agentique.domain.agents.GeomaticienAgent
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implémentation de l'agent Géomaticien : uniquement des requêtes SQL sur
 * pbf_index.db, aucun appel au SLM (cf. §4 de l'architecture).
 *
 * Stratégie de requête : un pré-filtre bounding-box (index B-Tree sur
 * lat/lon, rapide) suivi d'un affinage par distance exacte (Haversine) en
 * mémoire sur le petit sous-ensemble retourné — évite un scan complet de la
 * table sans dépendre du module R-Tree (non garanti sur toutes les
 * distributions Android, cf. remarque de cette étape).
 */
@Singleton
class PbfRepository @Inject constructor(
    private val dbOpener: PbfDatabaseOpener
) : GeomaticienAgent {

    private val db: SQLiteDatabase by lazy { dbOpener.open() }

    override suspend fun findNearestEntity(
        lat: Double,
        lon: Double,
        headingDegrees: Float
    ): AgentMessage.GeoResult? = withContext(Dispatchers.IO) {
        // Recherche initiale à 30 m ; élargie si rien trouvé — évite de
        // ramener des résultats sans rapport avec le regard de l'utilisateur.
        var radius = 30
        var candidates = queryBoundingBox(lat, lon, radius)
        while (candidates.isEmpty() && radius <= 200) {
            radius *= 2
            candidates = queryBoundingBox(lat, lon, radius)
        }
        candidates.minByOrNull { GeoMath.distanceMeters(lat, lon, it.lat, it.lon) }
            ?.toGeoResult(lat, lon)
    }

    override suspend fun findEntitiesInRadius(
        lat: Double,
        lon: Double,
        radiusMeters: Int
    ): List<AgentMessage.GeoResult> = withContext(Dispatchers.IO) {
        queryBoundingBox(lat, lon, radiusMeters)
            .map { it.toGeoResult(lat, lon) }
            .filter { it.distanceMeters <= radiusMeters }
    }

    override suspend fun resolveToponym(name: String): AgentMessage.GeoResult? =
        withContext(Dispatchers.IO) {
            // 1. Correspondance exacte d'abord — chemin rapide, le cas normal.
            exactMatch(name)?.let { return@withContext it.toGeoResult(it.lat, it.lon) }

            // 2. Sinon, correspondance approximative : Vosk a probablement
            // mal transcrit le toponyme (cf. limitation documentée sur les
            // phonèmes malgaches absents du modèle acoustique français).
            val query = name.lowercase()
            val maxDist = Levenshtein.maxToleratedDistance(query.length)

            var bestName: String? = null
            var bestDist = Int.MAX_VALUE
            for (candidate in allNames.value) {
                val dist = Levenshtein.distance(query, candidate.lowercase())
                if (dist < bestDist) {
                    bestDist = dist
                    bestName = candidate
                }
            }

            if (bestName != null && bestDist <= maxDist) {
                exactMatch(bestName)?.toGeoResult(0.0, 0.0)?.let { return@withContext it }
            }
            null
        }

    private fun exactMatch(name: String): Row? =
        db.query(
            PbfContract.TABLE,
            columns,
            "${PbfContract.COL_NAME} = ? COLLATE NOCASE",
            arrayOf(name),
            null, null,
            null,
            "1"
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.toRow() else null
        }

    // Liste de tous les noms distincts, chargée une seule fois en mémoire
    // (~16 000 chaînes courtes : négligeable) pour la comparaison floue.
    private val allNames = lazy {
        db.query(
            true, PbfContract.TABLE, arrayOf(PbfContract.COL_NAME),
            "${PbfContract.COL_NAME} IS NOT NULL", null,
            null, null, null, null
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.getString(0))
            }
        }
    }

    // --- Internes ---

    private val columns = arrayOf(
        PbfContract.COL_ID, PbfContract.COL_OSM_TYPE, PbfContract.COL_NAME,
        PbfContract.COL_TAGS_JSON, PbfContract.COL_LAT, PbfContract.COL_LON
    )

    private fun queryBoundingBox(lat: Double, lon: Double, radiusMeters: Int): List<Row> {
        val (latDelta, lonDelta) = GeoMath.degreeDeltaForRadius(radiusMeters, lat)
        val selection = """
            ${PbfContract.COL_LAT} BETWEEN ? AND ?
            AND ${PbfContract.COL_LON} BETWEEN ? AND ?
        """.trimIndent()
        val args = arrayOf(
            (lat - latDelta).toString(), (lat + latDelta).toString(),
            (lon - lonDelta).toString(), (lon + lonDelta).toString()
        )
        return db.query(PbfContract.TABLE, columns, selection, args, null, null, null)
            .use { cursor ->
                buildList {
                    while (cursor.moveToNext()) add(cursor.toRow())
                }
            }
    }

    private data class Row(
        val id: Long, val osmType: String, val name: String?,
        val tagsJson: String, val lat: Double, val lon: Double
    )

    private fun Cursor.toRow() = Row(
        id = getLong(0), osmType = getString(1), name = getStringOrNull(2),
        tagsJson = getString(3), lat = getDouble(4), lon = getDouble(5)
    )

    private fun Cursor.getStringOrNull(index: Int): String? =
        if (isNull(index)) null else getString(index)

    private fun Row.toGeoResult(fromLat: Double, fromLon: Double): AgentMessage.GeoResult {
        val tags = JSONObject(tagsJson)
        val map = buildMap { tags.keys().forEach { k -> put(k, tags.getString(k)) } }
        return AgentMessage.GeoResult(
            entityId = id,
            name = name ?: map["name"] ?: "inconnu",
            tags = map,
            distanceMeters = GeoMath.distanceMeters(fromLat, fromLon, lat, lon).toFloat()
        )
    }
}
