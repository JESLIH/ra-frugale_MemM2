cmake_minimum_required(VERSION 3.22.1)
project("ra_native")

# ---------------------------------------------------------------------------
# Squelette — à compléter à l'étape 5 de la feuille de route.
#
# Structure attendue une fois les sources clonées (cf. guide de
# téléchargement précédent) :
#   app/src/main/cpp/
#     ├── CMakeLists.txt        (ce fichier)
#     ├── llama.cpp/            (git clone ggerganov/llama.cpp)
#     ├── vosk-api/             (git clone alphacep/vosk-api)
#     ├── hnswlib/              (git clone nmslib/hnswlib, header-only)
#     └── jni_bridge.cpp        (le pont JNI que nous écrirons étape 5)
#
# Objectif de conception (cf. §1 et §6) : UNE SEULE bibliothèque native
# partagée (ra_native.so), pas trois runtimes JNI séparés — pour permettre
# un allocateur mémoire commun (arena) et le séquencement décrit en §6
# (libérer le contexte du SLM avant d'activer MediaCodec).
# ---------------------------------------------------------------------------

# add_subdirectory(llama.cpp)   # expose la cible `llama`
# add_subdirectory(vosk-api)    # à adapter : vosk-api n'a pas de CMake natif
#                                 pour Android par défaut, voir sa doc de build
# hnswlib est header-only : pas de add_subdirectory nécessaire, juste
# target_include_directories ci-dessous.

add_library(ra_native SHARED
    jni_bridge.cpp
)

# Support des pages 16 KB (exigé pour Android 15+ / Pixel 8+)
target_link_options(ra_native PRIVATE "-Wl,-z,max-page-size=16384")

target_include_directories(ra_native PRIVATE
    hnswlib
)

# target_link_libraries(ra_native PRIVATE
#     llama
#     vosk
#     log
#     android
# )

# NB : ne pas oublier, une fois llama.cpp intégré, de compiler avec les
# extensions ARM adaptées (-mcpu=... ou via les flags par défaut du CMake
# de llama.cpp pour NEON) — c'est un facteur direct du débit tokens/s
# mesuré en phase 1 (Xiao et al.).
