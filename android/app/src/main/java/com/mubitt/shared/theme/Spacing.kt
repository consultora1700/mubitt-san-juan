package com.mubitt.shared.theme

import androidx.compose.ui.unit.dp

/**
 * Mubitt Spacing System
 * 
 * Consistent spacing values for the entire app
 * Standards: Uber-level visual consistency and hierarchy
 * Based on 4dp grid system with semantic naming
 */

object Spacing {
    // Base unit - 4dp
    val base = 4.dp
    
    // Micro spacing
    val extraSmall = 4.dp    // 1x base - for tight spacing
    val small = 8.dp         // 2x base - for close elements
    
    // Standard spacing
    val medium = 16.dp       // 4x base - default spacing
    val large = 24.dp        // 6x base - section spacing
    val extraLarge = 32.dp   // 8x base - major sections
    
    // Macro spacing
    val jumbo = 48.dp        // 12x base - screen margins
    val huge = 64.dp         // 16x base - large separations
    
    // Semantic spacing aliases
    val padding = medium      // Default padding
    val margin = medium       // Default margin
    val gap = small          // Gap between related elements
    val section = large      // Between different sections
    val screen = jumbo       // Screen edge margins
    
    // Component-specific spacing
    val buttonPadding = medium
    val cardPadding = medium
    val listItemPadding = medium
    val bottomSheetPadding = large
    
    // Touch targets (minimum 48dp for accessibility)
    val minTouchTarget = 48.dp
    val touchTargetPadding = 12.dp  // (48 - 24) / 2 for 24dp content
}