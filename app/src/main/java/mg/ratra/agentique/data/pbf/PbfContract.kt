package mg.ratra.agentique.data.pbf

/**
 * Doit rester en miroir exact du schéma créé par tools/build_pbf_index.py.
 * Si vous changez l'un, changez l'autre.
 */
object PbfContract {
    const val DB_NAME = "pbf_index.db"
    const val TABLE = "osm_entities"

    const val COL_ID = "id"
    const val COL_OSM_TYPE = "osm_type"
    const val COL_NAME = "name"
    const val COL_TAGS_JSON = "tags_json"
    const val COL_LAT = "centroid_lat"
    const val COL_LON = "centroid_lon"
}
