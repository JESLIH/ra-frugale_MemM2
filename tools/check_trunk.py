import sys
import osmium

class TrunkCounter(osmium.SimpleHandler):
    def __init__(self):
        super().__init__()
        self.highway_counts = {}

    def way(self, w):
        hw = w.tags.get("highway")
        if hw:
            self.highway_counts[hw] = self.highway_counts.get(hw, 0) + 1

h = TrunkCounter()
h.apply_file("../madagascar-260924.osm.pbf")

print("Toutes les valeurs highway= trouvées dans la source brute :\n")
for k, v in sorted(h.highway_counts.items(), key=lambda x: -x[1]):
    print(f"  {k}: {v}")