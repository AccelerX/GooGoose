package com.example.googoose.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// `--radius-*` tokens from styles.css.
val RadiusSmall = 4.dp
val RadiusMedium = 8.dp
val RadiusLarge = 14.dp

val GooGooseShapes = Shapes(
    small = RoundedCornerShape(RadiusSmall),
    medium = RoundedCornerShape(RadiusMedium),
    large = RoundedCornerShape(RadiusLarge),
)
