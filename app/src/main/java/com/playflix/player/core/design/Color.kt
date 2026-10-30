package com.playflix.player.core.design

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Dark Navy & Obsidian Palette (Inspired by Reference UI)
val PlayFlixNavyDark = Color(0xFF070A12)
val PlayFlixNavySurface = Color(0xFF101524)
val PlayFlixCardSurface = Color(0xFF161C2E)
val PlayFlixCardElevated = Color(0xFF1E263D)
val PlayFlixBorderDark = Color(0xFF26304D)

// Pure White Light Mode Palette
val PlayFlixLightBackground = Color(0xFFF8F9FC)
val PlayFlixLightSurface = Color(0xFFFFFFFF)
val PlayFlixLightCard = Color(0xFFF0F3F9)
val PlayFlixBorderLight = Color(0xFFE2E8F0)

// Accent Colors (Purple, Violet, Electric Blue, Emerald, Gold)
val PlayFlixViolet = Color(0xFF7C4DFF)
val PlayFlixPurple = Color(0xFF9D4EDD)
val PlayFlixMagenta = Color(0xFFD946EF)
val PlayFlixBlue = Color(0xFF00B4D8)
val PlayFlixTeal = Color(0xFF10B981)
val PlayFlixAmber = Color(0xFFF59E0B)
val PlayFlixCoral = Color(0xFFF43F5E)

// Quick Access Card Tints
val QuickMusicCardBg = Color(0xFF231942)
val QuickVideoCardBg = Color(0xFF12263A)
val QuickFolderCardBg = Color(0xFF0F292A)
val QuickFavCardBg = Color(0xFF2D2318)

// Text Colors
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)
val TextMutedDark = Color(0xFF64748B)

val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)

object PlayFlixGradients {
    val PrimaryAccent = Brush.linearGradient(
        colors = listOf(PlayFlixViolet, PlayFlixPurple, PlayFlixMagenta)
    )

    val FeaturedBanner = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1D3557),
            Color(0xFF3A2D6A),
            Color(0xFF151B2D)
        )
    )

    val MiniPlayerSurface = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF181F34),
            Color(0xFF211C3A),
            Color(0xFF161C2E)
        )
    )
}
