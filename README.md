# Ma CDthèque — application Android

Projet Android Studio (Kotlin + Jetpack Compose) préchargé avec les 1 310 lignes du classeur fourni.

## Installation
1. Ouvrir ce dossier dans Android Studio (version récente, JDK 17).
2. Attendre la synchronisation Gradle (Internet requis).
3. Brancher un téléphone Android (Android 8.0+) et cliquer sur **Run**, ou choisir Build > Build APK(s) pour générer un APK.

## Fonctions
- Collection initiale embarquée, stockée ensuite en SQLite local.
- Recherche par titre, artiste, genre, référence et EAN.
- Scanner EAN via Google Code Scanner (Google Play Services requis).
- Ajout manuel ou après scan, suppression avec confirmation.
- Recherche de pochettes via MusicBrainz / Cover Art Archive, sur demande pour les anciens CD, automatiquement après ajout avec EAN.

## Limites de cette version
- Les données de titre/artiste des nouveaux scans ne sont pas automatiquement renseignées : saisie manuelle.
- L'URL d'une pochette peut exister sans image accessible; dans ce cas le chargement échoue silencieusement.
- Aucune synchronisation ni sauvegarde externe : penser à sauvegarder le téléphone avant une réinitialisation.
- Ce dossier est le **code source**, pas un APK compilé. Le build Android n'a pas été exécuté dans cet environnement.
