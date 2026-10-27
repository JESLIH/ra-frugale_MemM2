"""
Tire un échantillon reproductible de toponymes pour mesurer le taux de
reconnaissance vocale réel (protocole de test, citable dans le mémoire).

Usage :
    python sample_test_toponyms.py ..\\app\\src\\main\\assets\\malagasy_toponyms.json
"""
import json
import random
import sys

SEED = 42
N_PER_STRATE = 5

def main() -> None:
    if len(sys.argv) != 2:
        print("Usage: python sample_test_toponyms.py <malagasy_toponyms.json>", file=sys.stderr)
        sys.exit(1)

    with open(sys.argv[1], encoding="utf-8") as f:
        toponyms = json.load(f)

    short = [t for t in toponyms if len(t.split()) <= 2]
    long_ = [t for t in toponyms if len(t.split()) >= 3]

    rng = random.Random(SEED)
    sample = rng.sample(short, min(N_PER_STRATE, len(short))) + \
        rng.sample(long_, min(N_PER_STRATE, len(long_)))

    print(f"Échantillon de test (seed={SEED}, {len(short)} courts / {len(long_)} longs disponibles) :\n")
    for i, t in enumerate(sample, 1):
        print(f"{i:2d}. {t}")

if __name__ == "__main__":
    main()