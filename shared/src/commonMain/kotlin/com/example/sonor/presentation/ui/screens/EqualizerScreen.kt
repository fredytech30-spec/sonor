package com.example.sonor.presentation.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sonor.domain.model.PlayerState
import com.example.sonor.ui.theme.*

// ─── Data Models ───────────────────────────────────────────────────────────────

data class EqualizerPreset(
    val name: String,
    val icon: ImageVector,
    val levels: List<Float>
)

// ─── Equalizer Screen ──────────────────────────────────────────────────────────

@Composable
fun EqualizerScreen(
    playerState: PlayerState,
    onToggleEqualizer: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val bands = listOf("31", "63", "125", "250", "500", "1K", "2K", "4K", "8K", "16K")
    val levels = remember { mutableStateListOf(5f, 12f, -1f, 12f, -9f, 12f, 0f, -3f, 12f, 0f) }

    var showAllPresets by remember { mutableStateOf(false) }

    val presetsAll = listOf(
        EqualizerPreset("Normal",        Icons.Rounded.SentimentSatisfied, listOf(0f,0f,0f,0f,0f,0f,0f,0f,0f,0f)),
        EqualizerPreset("Pop",           Icons.Rounded.MusicNote,          listOf(1f,4f,6f,5f,3f,-1f,-2f,-1f,2f,4f)),
        EqualizerPreset("Hip-Hop",       Icons.Rounded.Headphones,         listOf(6f,8f,5f,-1f,-2f,1f,3f,5f,6f,5f)),
        EqualizerPreset("Rock n roll",   Icons.Rounded.MusicNote,          listOf(4f,6f,5f,3f,-1f,-2f,1f,3f,5f,6f)),
        EqualizerPreset("R&B",           Icons.Rounded.Person,             listOf(5f,7f,6f,3f,-2f,-1f,2f,4f,6f,5f)),
        EqualizerPreset("Folk",          Icons.Rounded.MusicNote,          listOf(3f,5f,4f,2f,0f,1f,3f,4f,5f,3f)),
        EqualizerPreset("Dance",         Icons.Rounded.MusicNote,          listOf(7f,9f,6f,2f,-3f,-1f,3f,5f,7f,6f)),
        EqualizerPreset("Jazz",          Icons.Rounded.MusicNote,          listOf(4f,6f,5f,3f,0f,1f,3f,5f,6f,4f)),
        EqualizerPreset("Classique",     Icons.Rounded.MusicNote,          listOf(3f,5f,4f,2f,1f,2f,3f,4f,5f,3f)),
        EqualizerPreset("Latin",         Icons.Rounded.MusicNote,          listOf(5f,7f,5f,2f,-1f,1f,3f,5f,7f,5f))
    )

    val presetsQuick = listOf(
        EqualizerPreset("Personnalisé(e)",       Icons.Rounded.Analytics,    levels.toList()),
        EqualizerPreset("Amplificateur basses",  Icons.Rounded.Speaker,      listOf(10f,12f,8f,5f,2f,0f,-1f,-2f,0f,1f)),
        EqualizerPreset("Booster vocal",         Icons.Rounded.Mic,          listOf(-2f,-1f,3f,6f,8f,6f,4f,2f,0f,-1f)),
        EqualizerPreset("Booster aigus",         Icons.Rounded.SurroundSound, listOf(-2f,-1f,0f,2f,4f,6f,8f,10f,12f,10f))
    )

    val activePresets = if (showAllPresets) presetsAll else presetsQuick

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnyxSurface)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        // ── Top Bar ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Retour",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = "Égaliseur",
                style = Typography.displayLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = WhitePure,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            )
            Switch(
                checked = playerState.equalizerEnabled,
                onCheckedChange = onToggleEqualizer,
                colors = SwitchDefaults.colors(
                    checkedThumbColor   = LarkAccent,
                    checkedTrackColor   = LarkAccent.copy(alpha = 0.5f),
                    uncheckedThumbColor = WhiteDim,
                    uncheckedTrackColor = DeepSpace
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── EQ Bands ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            levels.forEachIndexed { index, _ ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+", style = Typography.labelSmall, color = WhitePure, modifier = Modifier.height(20.dp))
                    Box(modifier = Modifier.height(200.dp)) {
                        EqualizerBand(
                            value = levels[index],
                            onValueChange = { levels[index] = it },
                            enabled = playerState.equalizerEnabled
                        )
                    }
                    Text(bands[index], style = Typography.labelMedium, color = WhiteMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Presets Grid ──────────────────────────────────────────────────────
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(activePresets) { preset ->
                EqualizerPresetCard(preset) {
                    levels.clear()
                    levels.addAll(preset.levels)
                }
            }
            if (!showAllPresets) {
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = DeepSpace,
                        border = androidx.compose.foundation.BorderStroke(1.dp, WhiteDim),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clickable { showAllPresets = true }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Rounded.ExpandMore, contentDescription = null, tint = WhiteMuted, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Voir plus", style = Typography.bodyLarge, color = WhiteMuted)
                        }
                    }
                }
            }
        }
    }
}

// ─── Equalizer Band ────────────────────────────────────────────────────────────

@Composable
fun EqualizerBand(
    value: Float,
    onValueChange: (Float) -> Unit,
    enabled: Boolean
) {
    Box(
        modifier = Modifier
            .width(24.dp)
            .height(200.dp)
            .background(DeepSpace, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Animated fill bar
        val animFill by animateFloatAsState(
            targetValue = (value + 15f) / 30f,
            animationSpec = spring(stiffness = Spring.StiffnessMedium),
            label = "eq_fill"
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(animFill)
                .background(
                    brush = if (enabled)
                        Brush.verticalGradient(listOf(LarkAccent, LarkAccent.copy(alpha = 0.4f)))
                    else
                        Brush.verticalGradient(listOf(WhiteDim, WhiteDim.copy(alpha = 0.2f))),
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
        )

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = -15f..15f,
            enabled = enabled,
            modifier = Modifier
                .fillMaxHeight()
                .width(24.dp)
                .graphicsLayer { rotationZ = -90f },
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            )
        )
    }
}

// ─── Preset Card ───────────────────────────────────────────────────────────────

@Composable
fun EqualizerPresetCard(
    preset: EqualizerPreset,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = OnyxSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, WhiteDim),
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        tonalElevation = 4.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                preset.icon,
                contentDescription = null,
                tint = WhitePure,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(preset.name, style = Typography.titleMedium, color = WhitePure)
        }
    }
}
