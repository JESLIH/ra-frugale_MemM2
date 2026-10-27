# RA Frugale — squelette de projet

Squelette initial du prototype "Système de Réalité Augmentée Agentique
Frugale pour un Système d'Information Ubiquitaire" (stage M2).

## Comment l'ouvrir

1. Ouvrir ce dossier directement dans Android Studio (Open an existing project).
2. Laisser Gradle synchroniser — il téléchargera les dépendances déclarées
   dans `app/build.gradle.kts`.
3. Installer le NDK et CMake via SDK Manager > SDK Tools si ce n'est pas
   déjà fait (nécessaire pour le module natif, même vide pour l'instant).
4. Le projet doit compiler et s'installer tel quel (écran vide) — c'est le
   point de départ vérifié avant d'ajouter la logique métier.

## Ce qui est déjà en place

- Structure de packages en Clean Architecture (`core/`, `data/`, `domain/`,
  `presentation/`) — reflète exactement l'architecture discutée.
- Contrats (`interface`) des deux agents et le protocole `AgentMessage`
  (`sealed class`), déjà typés et sérialisables.
- Squelette CMake/JNI qui compile (une seule fonction placeholder), prêt à
  accueillir llama.cpp, vosk-api et hnswlib.
- Manifeste avec les permissions capteurs/caméra/micro, sans permission
  Internet (cohérent avec offline-first).

## Ce qui n'est PAS encore fait (volontairement)

Chaque `TODO` dans le code renvoie à une étape de la feuille de route :

| Étape | Contenu | Fichiers concernés |
|---|---|---|
| 2 | Parsing `.pbf` + `pbf_index.db` (SQLite R-Tree) | `data/pbf/` |
| 3 | Intégration Vosk + grammaire contrainte | `domain/voice/`, ressources `assets/` |
| 4 | Implémentation des agents + `AgentCoordinator` | `domain/agents/` |
| 5 | llama.cpp + hnswlib en JNI, `LlmEngine`, `HnswIndexStore` | `data/llm/`, `data/rag/`, `cpp/` |
| 6 | Capteurs (EKF/UKF), rendu Camera2/WebView | `domain/sensorfusion/`, `presentation/ar/` |
| 7 | Profilage mémoire/batterie, séquencement | `core/`, `AgentCoordinator` |

## Vérification avant de continuer

Avant de passer à l'étape 2, confirmez que :
- [ ] Le projet compile sans erreur dans Android Studio
- [ ] L'app s'installe et se lance (écran vide, c'est normal)
- [ ] Le NDK/CMake est fonctionnel (pas d'erreur sur le module `ra_native`)

Une fois ces trois points validés sur votre machine, on attaque l'étape 2 :
l'extraction/filtrage du `.pbf` et la construction de `pbf_index.db`.
