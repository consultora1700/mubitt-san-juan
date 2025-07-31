package com.mubitt.shared.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Mubitt Color System - San Juan Premium Theme
 * 
 * Designed specifically for San Juan market
 * Standards: Indistinguishable from Uber quality
 * Colors inspired by San Juan landscape and culture
 */

// Primary Colors - San Juan Green inspired by local vineyards
val MubittGreen = Color(0xFF006C4C)
val MubittGreenLight = Color(0xFF89F8C7)
val MubittGreenDark = Color(0xFF004D35)

// Secondary Colors - Complementary earth tones
val MubittBrown = Color(0xFF4F6354)
val MubittBrownLight = Color(0xFF7B9284)
val MubittBrownDark = Color(0xFF2A3529)

// Accent Colors
val MubittOrange = Color(0xFFFF8C42)  // For highlights and actions
val MubittBlue = Color(0xFF2196F3)    // For information
val MubittRed = Color(0xFFBA1A1A)     // For errors and warnings

// Neutral Colors
val MubittGray50 = Color(0xFFFBFDF9)
val MubittGray100 = Color(0xFFF0F4F0)
val MubittGray200 = Color(0xFFE1E8E1)
val MubittGray300 = Color(0xFFCAD5CA)
val MubittGray400 = Color(0xFF9BA89B)
val MubittGray500 = Color(0xFF6B776B)
val MubittGray600 = Color(0xFF53635D)
val MubittGray700 = Color(0xFF3C4B41)
val MubittGray800 = Color(0xFF283228)
val MubittGray900 = Color(0xFF111F15)

// Light Color Scheme
val LightColorScheme = lightColorScheme(
    primary = MubittGreen,
    onPrimary = Color.White,
    primaryContainer = MubittGreenLight,
    onPrimaryContainer = MubittGreenDark,
    
    secondary = MubittBrown,
    onSecondary = Color.White,
    secondaryContainer = MubittBrownLight,
    onSecondaryContainer = MubittBrownDark,
    
    tertiary = MubittOrange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDCC5),
    onTertiaryContainer = Color(0xFF8B2900),
    
    error = MubittRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    background = MubittGray50,
    onBackground = MubittGray900,
    surface = MubittGray50,
    onSurface = MubittGray900,
    surfaceVariant = MubittGray100,
    onSurfaceVariant = MubittGray700,
    
    outline = MubittGray400,
    outlineVariant = MubittGray200,
    scrim = Color.Black.copy(alpha = 0.6f),
    
    inverseSurface = MubittGray800,
    inverseOnSurface = MubittGray100,
    inversePrimary = MubittGreenLight
)

// Dark Color Scheme
val DarkColorScheme = darkColorScheme(
    primary = MubittGreenLight,
    onPrimary = MubittGreenDark,
    primaryContainer = MubittGreen,
    onPrimaryContainer = MubittGreenLight,
    
    secondary = MubittBrownLight,
    onSecondary = MubittBrownDark,
    secondaryContainer = MubittBrown,
    onSecondaryContainer = MubittBrownLight,
    
    tertiary = Color(0xFFFFB886),
    onTertiary = Color(0xFF5C1900),
    tertiaryContainer = Color(0xFF7F2C00),
    onTertiaryContainer = Color(0xFFFFDCC5),
    
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    background = MubittGray900,
    onBackground = MubittGray100,
    surface = MubittGray900,
    onSurface = MubittGray100,
    surfaceVariant = MubittGray800,
    onSurfaceVariant = MubittGray300,
    
    outline = MubittGray500,
    outlineVariant = MubittGray700,
    scrim = Color.Black.copy(alpha = 0.8f),
    
    inverseSurface = MubittGray100,
    inverseOnSurface = MubittGray800,
    inversePrimary = MubittGreen
)