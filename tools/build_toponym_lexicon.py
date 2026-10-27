import json
import re
import sqlite3
import sys

MAX_WORDS = 8
QUOTE_CHARS = '"\'“”‘’'


def clean(raw: str):
    name = raw.strip().strip(QUOTE_CHARS).strip()
    if not name:
        return None
    if name.startswith("#"):
        return None
    if not any(c.isalpha() for c in name):
        return None
    if len(name.split()) > MAX_WORDS:
        return None
    return name.lower()


def main() -> None:
    if len(sys.argv) != 3:
        print("Usage: python build_toponym_lexicon.py <pbf_index.db> <out.json>", file=sys.stderr)
        sys.exit(1)

    db_path, out_path = sys.argv[1], sys.argv[2]
    conn = sqlite3.connect(db_path)
    cur = conn.cursor()

    rows = cur.execute(
        "SELECT DISTINCT name FROM osm_entities WHERE name IS NOT NULL AND name != ''"
    ).fetchall()

    kept = set()
    rejected_count = 0
    for (raw,) in rows:
        cleaned = clean(raw)
        if cleaned:
            kept.add(cleaned)
        else:
            rejected_count += 1

    toponyms = sorted(kept)

    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(toponyms, f, ensure_ascii=False, indent=2)

    print(f"OK : {len(toponyms)} toponymes conservés, {rejected_count} rejetés -> {out_path}")


if __name__ == "__main__":
    main()