package org.example.project.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Centralized design tokens for the SofaMobile app.
 * Ensures consistent spacing, shapes, and elevation across all screens and components.
 */
object AppDesign {
    // --- Shapes ---
    val CardShape = RoundedCornerShape(16.dp)
    val CardShapeSmall = RoundedCornerShape(12.dp)
    val ButtonShape = RoundedCornerShape(12.dp)
    val ChipShape = RoundedCornerShape(8.dp)
    val BarShape = RoundedCornerShape(6.dp)
    val InputShape = RoundedCornerShape(12.dp)

    // --- Spacing ---
    val ScreenPadding = 20.dp
    val SectionSpacing = 20.dp
    val ContentPadding = 16.dp
    val CardInnerPadding = 16.dp
    val ItemSpacing = 12.dp
    val SmallSpacing = 8.dp
    val TinySpacing = 4.dp

    // --- Elevation ---
    val CardElevation = 2.dp
    val CardElevationHigh = 4.dp
    val HeaderElevation = 6.dp

    // --- Sizes ---
    val BarHeight = 10.dp
    val BarHeightSmall = 6.dp
    val MiniBarHeight = 4.dp
    val IconSizeSmall = 20.dp
    val IconSizeMedium = 24.dp

    // --- Max widths for web responsiveness ---
    val MaxContentWidth = 900.dp
}
