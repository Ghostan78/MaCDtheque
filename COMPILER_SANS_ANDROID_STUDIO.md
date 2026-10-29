# Créer l'APK avec GitHub Actions

1. Créez un compte GitHub sur https://github.com.
2. Créez un dépôt privé vide, par exemple `MaCDtheque`.
3. Importez le contenu du dossier `MaCDtheque` à la racine du dépôt, **y compris** `.github/workflows/android-apk.yml` (dossier masqué).
4. Dans GitHub, ouvrez Actions > Construire APK Android > Run workflow.
5. Attendez la fin du workflow. Dans la page de l'exécution, sous Artifacts, téléchargez `MaCDtheque-APK`.
6. Décompressez ce téléchargement pour récupérer `app-debug.apk`.
7. Copiez `app-debug.apk` sur le téléphone Android, ouvrez-le et autorisez si nécessaire l'installation depuis cette source.

L'APK est une version de test (debug), non destinée à être publiée sur le Play Store. La compilation et le fonctionnement sur téléphone n'ont pas été vérifiés dans cet environnement. Un échec de compilation nécessitera de corriger le code source et de relancer le workflow.
