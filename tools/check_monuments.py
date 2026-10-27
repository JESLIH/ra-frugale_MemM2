import sqlite3, json
from collections import Counter

conn = sqlite3.connect("../app/src/main/assets/pbf_index.db")
cur = conn.cursor()

historic_types = Counter()
highway_types = Counter()
national_roads = 0

for (tags_json,) in cur.execute("SELECT tags_json FROM osm_entities"):
    tags = json.loads(tags_json)

    if "historic" in tags:
        historic_types[tags["historic"]] += 1

    if "highway" in tags:
        highway_types[tags["highway"]] += 1

    ref = tags.get("ref", "")
    if tags.get("highway") in ("trunk", "primary") and ref.upper().startswith("RN"):
        national_roads += 1

print("=== Patrimoine historique (historic=*) ===")
for k, v in historic_types.most_common():
    print(f"  {k}: {v}")

print("\n=== Voies (highway=*) ===")
for k, v in highway_types.most_common():
    print(f"  {k}: {v}")

print(f"\nRoutes nationales détectées (ref commençant par RN): {national_roads}")