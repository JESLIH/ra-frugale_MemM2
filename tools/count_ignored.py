# tools/count_ignored.py
import re
import os
import sys

candidate_paths = [
    "ignored_words.txt",
    "../ignored_words.txt",
    "tools/ignored_words.txt",
    os.path.join(os.path.dirname(__file__), "ignored_words.txt"),
    os.path.join(os.path.dirname(__file__), "..", "ignored_words.txt")
]

target_file = None
for p in candidate_paths:
    if os.path.exists(p):
        target_file = p
        break

if not target_file:
    print("Erreur : Fichier 'ignored_words.txt' introuvable.")
    print("Pour générer ce fichier, redirigez la sortie des logs Vosk (ex. logcat) :")
    print("  adb logcat | Select-String 'Ignoring word' > ignored_words.txt")
    sys.exit(1)

seen = set()
with open(target_file, encoding="utf-8", errors="ignore") as f:
    for line in f:
        m = re.search(r"Ignoring word missing in vocabulary: '([^']+)'", line)
        if m:
            seen.add(m.group(1))

print(f"Fichier analysé : {target_file}")
print(f"{len(seen)} mots uniques ignorés sur 15842 toponymes")
print(f"Soit {100 * len(seen) / 15842:.1f} % de la grammaire inutilisable")
