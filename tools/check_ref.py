import osmium

class RefChecker(osmium.SimpleHandler):
    def __init__(self):
        super().__init__()
        self.named = 0
        self.ref_only = 0
        self.neither = 0
        self.ref_samples = []

    def way(self, w):
        if w.tags.get("highway") not in ("primary", "secondary", "tertiary", "trunk"):
            return
        name = w.tags.get("name")
        ref = w.tags.get("ref")
        if name:
            self.named += 1
        elif ref:
            self.ref_only += 1
            if len(self.ref_samples) < 10:
                self.ref_samples.append(ref)
        else:
            self.neither += 1

h = RefChecker()
h.apply_file("../madagascar-260924.osm.pbf")

print("Avec name:", h.named)
print("Avec ref seulement (sans name):", h.ref_only)
print("Ni l'un ni l'autre:", h.neither)
print("Exemples de ref:", h.ref_samples)