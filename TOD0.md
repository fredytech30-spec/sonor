# TODO - Corrections KMP (P0)

- [ ] Unifier PlayerState: supprimer `PlayerState` de `MusicController.kt`, utiliser `com.example.sonor.domain.model.PlayerState`
- [ ] Mettre à jour `MusicController` (interface) pour exposer le bon `PlayerState`
- [ ] Mettre à jour `MusicControllerImpl.android` pour produire le `domain.model.PlayerState`
- [ ] Modifier `HomeViewModel` pour injecter `MusicController` et piloter la lecture via controller (pas de state local incohérent)
- [ ] Corriger Koin: `SharedModule` / `AppModule` pour une seule définition cohérente de `HomeViewModel`
- [ ] Stabiliser play/pause/seek/next/prev dans `HomeViewModel` + controller
- [ ] Puis refactor critique Android: `MediaStore.*.DATA` -> utiliser `content://` URI (scoped storage)
