package com.example.sonor.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sonor.domain.model.Song
import com.example.sonor.ui.theme.*

/**
 * Bottom sheet with contextual actions for a given song.
 * Uses the KMP-compatible Song model (no Android-specific types).
 * The album art is shown as a styled placeholder since Coil's AsyncImage
 * requires a platform-specific URL — integrate it in your Android/Desktop actual if needed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongActionSheet(
    song: Song,
    isFavorite: Boolean = false,
    onDismiss: () -> Unit,
    onAction: (SongAction) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = OnyxSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = WhiteDim) }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            // ── Header: Song Info ──────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Artwork placeholder
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = DeepSpace
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = WhiteDim,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Column(Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = WhitePure,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = Typography.bodyMedium,
                        color = WhiteMuted,
                        maxLines = 1
                    )
                }

                IconButton(onClick = { onAction(SongAction.EditTags) }) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Modifier les tags", tint = WhiteMuted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(4.dp))

            // ── Actions ────────────────────────────────────────────────────────
            ActionRow(
                icon     = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                title    = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                iconTint = if (isFavorite) FavoriteRed else WhitePure,
                textTint = if (isFavorite) FavoriteRed else WhitePure
            ) { onAction(SongAction.ToggleFavorite) }

            ActionRow(Icons.Rounded.PlaylistAdd,    "Ajouter à une playlist")   { onAction(SongAction.AddToPlaylist) }
            ActionRow(Icons.Rounded.Share,           "Partager")                { onAction(SongAction.Share) }
            ActionRow(Icons.Rounded.Speed,           "Vitesse de lecture")      { onAction(SongAction.PlaybackSpeed) }
            ActionRow(Icons.Rounded.Notifications,   "Définir comme sonnerie")  { onAction(SongAction.SetAsRingtone) }
            ActionRow(Icons.Rounded.Schedule,        "Minuterie d'arrêt")       { onAction(SongAction.SleepTimer) }
            ActionRow(Icons.Rounded.BatterySaver,    "Mode économie d'énergie") { onAction(SongAction.EnergySaver) }
            ActionRow(
                icon     = Icons.Rounded.VisibilityOff,
                title    = "Masquer",
                iconTint = FavoriteRed,
                textTint = FavoriteRed
            ) { onAction(SongAction.Hide) }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// ─── Song Action Sealed Class ──────────────────────────────────────────────────

sealed class SongAction {
    data object ToggleFavorite : SongAction()
    data object AddToPlaylist  : SongAction()
    data object Share          : SongAction()
    data object PlaybackSpeed  : SongAction()
    data object SetAsRingtone  : SongAction()
    data object SleepTimer     : SongAction()
    data object EnergySaver    : SongAction()
    data object Hide           : SongAction()
    data object EditTags       : SongAction()
}

// ─── Action Row ────────────────────────────────────────────────────────────────

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    iconTint: Color = WhitePure,
    textTint: Color = WhitePure,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text  = title,
            style = Typography.bodyLarge,
            color = textTint
        )
    }
}
