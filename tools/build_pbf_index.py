"""
Construit pbf_index.db à partir d'un extrait .pbf.

Ce script tourne UNE FOIS sur votre PC, jamais sur le téléphone. Le fichier
.db produit est ensuite copié dans app/src/main/assets/pbf_index.db pour
être embarqué dans l'APK.

Usage :
    pip install osmium
    python build_pbf_index.py madagascar-260924.osm.pbf pbf_index.db
"""

import json
import os
import sqlite3
import sys

import osmium

BATCH_SIZE = 5000

RELEVANT_KEYS = {"building", "historic", "tourism", "amenity"}

NAMED_ROAD_VALUES = {
    "trunk", "residential", "tertiary", "secondary", "primary",
    "unclassified", "living_street",
}

TRAIL_VALUES = {"footway", "path", "track"}
TRAIL_DESCRIPTOR_KEYS = {"surface", "sac_scale", "trail_visibility", "incline"}

REQUIRE_NAME = True


class EntityHandler(osmium.SimpleHandler):
    def __init__(self, db_path: str):
        super().__init__()
        self.conn = sqlite3.connect(db_path)
        self._init_schema()
        self.batch: list[tuple] = []
        self.count = 0

    def _init_schema(self) -> None:
        cur = self.conn.cursor()
        cur.execute("""
            CREATE TABLE IF NOT EXISTS osm_entities (
                id INTEGER PRIMARY KEY,
                osm_type TEXT NOT NULL,
                name TEXT,
                tags_json TEXT NOT NULL,
                centroid_lat REAL NOT NULL,
                centroid_lon REAL NOT NULL
            )
        """)
        cur.execute("CREATE INDEX IF NOT EXISTS idx_lat ON osm_entities(centroid_lat)")
        cur.execute("CREATE INDEX IF NOT EXISTS idx_lon ON osm_entities(centroid_lon)")
        self.conn.commit()

    def node(self, n: osmium.osm.Node) -> None:
        if n.location.valid():
            self._handle(n, "node", n.location.lat, n.location.lon)

    def way(self, w: osmium.osm.Way) -> None:
        lats = [nd.location.lat for nd in w.nodes if nd.location.valid()]
        lons = [nd.location.lon for nd in w.nodes if nd.location.valid()]
        if not lats:
            return
        self._handle(w, "way", sum(lats) / len(lats), sum(lons) / len(lons))

    def _handle(self, obj, osm_type: str, lat: float, lon: float) -> None:
        tags = {t.k: t.v for t in obj.tags}
        highway = tags.get("highway")
        name = tags.get("name")
        ref = tags.get("ref")

        if highway in TRAIL_VALUES:
            if not TRAIL_DESCRIPTOR_KEYS.intersection(tags.keys()):
                return  # trait anonyme sans info exploitable : rejeté

        elif highway in NAMED_ROAD_VALUES:
            if not name and ref:
                name = ref  # ex. "N 4" pour une route nationale sans nom propre
            if REQUIRE_NAME and not name:
                return

        elif RELEVANT_KEYS.intersection(tags.keys()):
            if REQUIRE_NAME and not name:
                return

        else:
            return  # rien de pertinent pour ce projet

        self.batch.append(
            (obj.id, osm_type, name, json.dumps(tags, ensure_ascii=False), lat, lon)
        )
        if len(self.batch) >= BATCH_SIZE:
            self._flush()

    def _flush(self) -> None:
        if not self.batch:
            return
        self.conn.executemany(
            "INSERT OR REPLACE INTO osm_entities VALUES (?,?,?,?,?,?)",
            self.batch,
        )
        self.conn.commit()
        self.count += len(self.batch)
        print(f"  ... {self.count} entités écrites", file=sys.stderr)
        self.batch = []

    def close(self) -> None:
        self._flush()
        self.conn.close()


def main() -> None:
    if len(sys.argv) != 3:
        print("Usage: python build_pbf_index.py <in.osm.pbf> <out.db>", file=sys.stderr)
        sys.exit(1)

    input_pbf, output_db = sys.argv[1], sys.argv[2]
    if os.path.exists(output_db):
        os.remove(output_db)
        print(f"Ancien {output_db} supprimé, reconstruction complète.", file=sys.stderr)

    handler = EntityHandler(output_db)
    handler.apply_file(input_pbf, locations=True, idx="flex_mem")
    handler.close()
    print(f"OK : {output_db} généré ({handler.count} entités)")


if __name__ == "__main__":
    main()