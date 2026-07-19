package com.example.sonor.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sonor.ui.theme.*

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onEqualizerClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "PROFIL",
            style = Typography.displayLarge.copy(
                fontSize = 32.sp,
                letterSpacing = 8.sp,
                brush = Brush.linearGradient(LuxuryGoldGradient)
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Profile Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(AuroraGradient)),
                contentAlignment = Alignment.Center
            ) {
                Text("JD", style = Typography.headlineMedium, color = WhitePure)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text("Jean Dupont", style = Typography.titleLarge, color = WhitePure)
                Text("Membre Sonor Gold", style = Typography.labelMedium, color = SonorGoldLiquid)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Settings Groups
        SettingsItem(Icons.Rounded.Person, "Mon Compte") { }
        SettingsItem(Icons.Rounded.CloudDownload, "Téléchargements") { }
        SettingsItem(Icons.Rounded.Tune, "Égaliseur Audio", onClick = onEqualizerClick)
        SettingsItem(Icons.Rounded.Shield, "Confidentialité") { }
        
        Spacer(modifier = Modifier.weight(1f))

        // Logout
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GlassSurface),
            shape = Shapes.medium
        ) {
            Text("DÉCONNEXION", color = FavoriteRed, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = WhiteDim, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(20.dp))
        Text(title, style = Typography.bodyLarge, color = WhitePure, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = WhiteDim)
    }
}
