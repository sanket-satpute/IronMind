package com.sanket_satpute_20.ironmind.home

import androidx.compose.ui.graphics.Color
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.TextMuted

// Home is a calm command deck: one warm action color, one cool focus color,
// and muted reward/status colors. Avoid using several neon accents at once.
internal val HomeCanvas = Color(0xFF0B0E13)
internal val HomeSurface = Color(0xFF141A22)
internal val HomeSurfaceRaised = Color(0xFF1A222D)
internal val HomeOutline = Color(0xFF2A3544)
internal val HomeAction = Color(0xFFFF714B)
internal val HomeFocus = Color(0xFF69D6E8)
internal val HomeReward = Color(0xFFE6C86E)
internal val HomeComplete = Color(0xFF55CF91)
internal val HomeTextPrimary = Color(0xFFF4F7FB)
internal val HomeTextSecondary = Color(0xFFAAB4C3)

// Compatibility tokens for the existing Home sections. These now map to their
// semantic design-system values rather than accent colors masquerading as text.
internal val SurfaceLighter = SurfaceElevated
internal val TextSecondary = TextMuted
internal val DarkGrey = SurfaceDark
