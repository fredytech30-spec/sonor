# Plan de Migration Kotlin Multiplatform (KMP) - Sonor

## Vue d'ensemble
Ce document décrit le processus de migration de l'application Sonor vers Kotlin Multiplatform pour supporter Android et Desktop avec un maximum de code partagé.

## État Actuel de la Migration

### ✅ Complété

1. **Structure KMP configurée**
   - Module `shared` avec targets Android et Desktop
   - Structure de dossiers créée dans `shared/src/commonMain/kotlin/com/example/sonor/`
   - Dossiers platform-specific créés (`androidMain`, `desktopMain`)

2. **Modèles de données migrés vers shared**
   - `Song.kt` (déjà existant)
   - `PlayerState.kt` (mis à jour avec `currentSongId`)
   - `Artist.kt` (nouveau)
   - `Album.kt` (nouveau)
   - `Entities.kt` (nouveau - toutes les entités database)
   - `PlatformFile.kt` (nouveau avec expect/actual)

3. **Interfaces repository migrées**
   - `SongRepository.kt` (interface complète)
   - `AuthRepository.kt` (interface complète)
   - `DatabaseHelper.kt` (interface pour l'accès base de données)

4. **Expect/Actual pour APIs platform-specific**
   - `PlatformFile.kt` (common)
   - `PlatformFile.android.kt` (implémentation Android)
   - `PlatformFile.desktop.kt` (implémentation Desktop)

5. **Dépendances KMP configurées**
   - Ajout de `lifecycle-viewmodel` et `lifecycle-viewmodel-compose`
   - Ajout de `navigation-multiplatform`
   - Ajout de `sql-delight` pour la base de données cross-platform
   - Configuration du module `shared` avec les nouvelles dépendances
   - Configuration du module `desktop` avec les dépendances nécessaires
   - Plugin SQLDelight configuré dans `build.gradle.kts`

6. **ViewModels migrés vers shared**
   - `HomeViewModel.kt` (ViewModel KMP avec logique de lecture)
   - `AuthViewModel.kt` (ViewModel KMP pour l'authentification)

7. **Composants UI communs migrés**
   - `Screen.kt` (navigation multiplateforme)
   - `MiniPlayer.kt` (composant UI partagé)

8. **Module Desktop configuré**
   - `Main.kt` mis à jour avec les imports corrects
   - `build.gradle.kts` du desktop mis à jour

9. **Implémentation Repositories**
   - `SongRepositoryImpl.android.kt` (implémentation Android avec MediaStore)
   - `SongRepositoryImpl.desktop.kt` (implémentation Desktop avec File API)
   - `DatabaseHelperImpl.android.kt` (implémentation Android avec Room)
   - `DatabaseHelperImpl.desktop.kt` (implémentation Desktop avec SQLDelight)
   - `SonorDatabase.kt` (schéma Room copié dans androidMain)

10. **Base de données SQLDelight**
    - `SonorDatabase.sq` (schéma SQLDelight complet)
    - Configuration du plugin SQLDelight

11. **Music Controller**
    - `MusicController.kt` (interface commune)
    - `MusicControllerImpl.android.kt` (implémentation Android avec Media3)
    - `MusicControllerImpl.desktop.kt` (implémentation Desktop - placeholder)

12. **Dependency Injection (Koin)**
    - `SharedModule.kt` (module DI commun)
    - `AndroidModule.kt` (module DI Android spécifique)
    - `DesktopModule.kt` (module DI Desktop spécifique)

## 🔄 À Faire (Prochaines Étapes)

### Étape 1: Migration des Écrans UI

Migrer progressivement les écrans vers shared:
1. `HomeScreen.kt`
2. `SearchScreen.kt`
3. `LibraryScreen.kt`
4. `SettingsScreen.kt`
5. Écrans secondaires (Artist, Album, Playlist, etc.)

### Étape 2: Intégration avec le Module Android

- Mettre à jour `MainActivity.kt` pour utiliser les ViewModels partagés
- Configurer Koin dans l'application Android
- Intégrer le MusicController Android avec le service existant
- Adapter les écrans Android existants pour utiliser le code partagé

### Étape 3: Intégration avec le Module Desktop

- Initialiser Koin dans `Main.kt`
- Connecter l'interface Desktop avec les ViewModels partagés
- Implémenter la navigation Desktop
- Adapter l'UI pour les différentes tailles d'écran

### Étape 4: Tests et Validation

- Tester la compilation Android
- Tester la compilation Desktop
- Valider les fonctionnalités sur les deux plateformes
- Tests d'intégration pour les repositories
- Tests UI pour les composants partagés

### Étape 5: Améliorations Desktop

- Intégrer une vraie bibliothèque audio (VLCJ, JavaFX Media, ou similaire)
- Implémenter l'extraction des métadonnées audio
- Optimiser les performances pour Desktop

## Structure Cible du Projet

```
Sonor/
├── app/                          # Module Android (UI Android spécifique)
│   └── src/main/
│       ├── java/com/example/sonor/
│       │   ├── MainActivity.kt   # Point d'entrée Android
│       │   ├── SonorApp.kt      # Configuration Hilt
│       │   ├── audio/           # Service Media3 (Android only)
│       │   ├── data/            # Implémentation Room (Android only)
│       │   └── presentation/     # Écrans Android spécifiques
│       └── res/                 # Ressources Android
│
├── desktop/                      # Module Desktop
│   └── src/main/
│       └── kotlin/Main.kt       # Point d'entrée Desktop
│
└── shared/                       # Module partagé KMP
    ├── src/
    │   ├── commonMain/           # Code commun
    │   │   └── kotlin/com/example/sonor/
    │   │       ├── domain/
    │   │       │   ├── model/          # Modèles partagés
    │   │       │   └── repository/    # Interfaces repository
    │   │       ├── data/
    │   │       │   └── repository/    # Implémentations communes
    │   │       ├── presentation/
    │   │       │   ├── viewmodel/     # ViewModels KMP
    │   │       │   └── ui/            # Composants UI partagés
    │   │       ├── platform/          # Déclarations expect
    │   │       └── di/                # Configuration DI Koin
    │   │
    │   ├── androidMain/               # Code Android spécifique
    │   │   └── kotlin/com/example/sonor/
    │   │       ├── platform/          # Implémentations actual
    │   │       ├── data/              # Room, MediaStore
    │   │       └── audio/             # Media3
    │   │
    │   └── desktopMain/              # Code Desktop spécifique
    │       └── kotlin/com/example/sonor/
    │           ├── platform/          # Implémentations actual
    │           ├── data/              # SQLDelight, File API
    │           └── audio/             # VLC/JavaFX
    │
    └── build.gradle.kts              # Configuration KMP
```

## Dépendances Principales

### CommonMain
- Compose Multiplatform (UI)
- Kotlinx Coroutines (async)
- Kotlinx DateTime (dates)
- Koin (DI)
- Kamel (images)
- Lifecycle ViewModel (ViewModels KMP)
- Navigation Multiplatform (navigation)

### AndroidMain
- Room (base de données)
- Media3 (lecture audio)
- DataStore (préférences)
- Coil (images)

### DesktopMain
- SQLDelight (base de données)
- VLCJ ou JavaFX Media (lecture audio)
- File API standard (fichiers)

## Stratégie de Migration Progressive

### Phase 1: Fondation (✅ Complété)
- Structure KMP
- Modèles de données
- Interfaces repository
- Configuration des dépendances

### Phase 2: Logique Métier (En cours)
- Implémentation des repositories platform-specific
- Migration des ViewModels
- Création des expect/actual pour les APIs système

### Phase 3: UI Partagée
- Migration des composants UI communs
- Configuration de la navigation multiplateforme
- Adaptation des thèmes et styles

### Phase 4: Intégration
- Intégration avec les plateformes spécifiques
- Configuration de la DI
- Tests et validation

## Notes Importantes

1. **Compatibilité**: Maintenir la compatibilité avec le code Android existant pendant la transition
2. **Tests**: Tester chaque étape sur les deux plateformes
3. **Performance**: Surveiller les performances sur Desktop
4. **UI**: Adapter l'UI pour les différentes tailles d'écran
5. **Permissions**: Gérer les différences de permissions entre Android et Desktop

## Problèmes Connus

- **Réseau**: Problèmes de connectivité pour le téléchargement des dépendances Gradle (à résoudre)
- **Toolchain Java**: Configuration nécessaire pour utiliser Java 17 au lieu de Java 21

## Ressources

- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
- [SQLDelight](https://cashapp.github.io/sqldelight/)
- [Koin Documentation](https://insert-koin.io/docs/getting-started/introduction)
