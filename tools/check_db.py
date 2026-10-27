import sqlite3, json

conn = sqlite3.connect("../app/src/main/assets/pbf_index.db")
cur = conn.cursor()

print("total:", cur.execute("SELECT COUNT(*) FROM osm_entities").fetchone()[0])

trails = 0
for (tags_json,) in cur.execute("SELECT tags_json FROM osm_entities"):
    if json.loads(tags_json).get("highway") in ("footway", "path", "track"):
        trails += 1
print("sentiers (footway/path/track):", trails)