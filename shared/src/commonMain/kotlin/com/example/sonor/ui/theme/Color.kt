package com.example.sonor.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Lark Player Inspired Palette
 * Deep dark green/black tones with bright blue accent
 */

val MidnightBlack = Color(0xFF000000) 
val OnyxSurface = Color(0xFF0F1A15) // Deep dark green-black
val DeepSpace = Color(0xFF1A2620)
val LarkAccent = Color(0xFF00A8FF) // Bright blue accent for active states
val LarkAccentMuted = Color(0xFF0088CC)
val LarkTeal = Color(0xFF00B894) // Optional teal

// Typography & États
val WhitePure = Color(0xFFFFFFFF)
val WhiteMuted = Color(0x99FFFFFF)
val WhiteDim = Color(0x4DFFFFFF)
val GlassSurface = Color(0x1AFFFFFF)

// Gradients
val LarkBlueGradient = listOf(LarkAccent, LarkAccentMuted)
val LarkTealGradient = listOf(LarkTeal, LarkAccent)
val GlassGradient = listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.04f))

// Functional Colors
val FavoriteRed = Color(0xFFFF2D55)
val SuccessGreen = Color(0xFF4CD964)
val SonorGoldLiquid = Color(0xFFF7CE68) // Keep for compatibility
val SonorGoldDeep = Color(0xFFFBAB7E)
val LuxuryGoldGradient = listOf(SonorGoldDeep, SonorGoldLiquid)

// Additional Colors for Login Screen
val SonorPurpleCosmic = Color(0xFF8E2DE2)
val SonorCyanElectric = Color(0xFF4A00E0)
val AuroraGradient = listOf(SonorPurpleCosmic, SonorCyanElectric)
val SunsetGradient = listOf(Color(0xFFFF512F), Color(0xFFF09819))
