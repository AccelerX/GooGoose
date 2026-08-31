package com.example.googoose.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.example.googoose.data.TextSizePreset

// The mockup only designs a dark theme (android-frame.jsx renders it with
// dark="true" and styles.css defines a single dark palette) — v1 ships that
// one theme only, no light variant and no Android 12+ dynamic color.
private val GooGooseDarkColorScheme = darkColorScheme(
    background = NocturneBg,
    onBackground = NocturneText,
    surface = NocturneSurface,
    onSurface = NocturneText,
    surfaceVariant = NocturneNeutral800,
    onSurfaceVariant = NocturneNeutral400,
    primary = NocturneAccent,
    onPrimary = NocturneAccent900,
    primaryContainer = NocturneAccent800,
    onPrimaryContainer = NocturneAccent100,
    secondary = NocturneAccent,
    outline = NocturneDivider,
    outlineVariant = NocturneDivider,
)

/** Material3's few default-styled internals (dropdown menu items, DatePicker digits) — kept in sync with the same [TextSizePreset] as [GooGooseType]. */
private fun materialTypography(t: GooGooseTypography): Typography = Typography(
    bodyLarge = t.body,
    bodyMedium = t.bodySmall,
    bodySmall = t.label,
    titleLarge = t.h3,
    titleMedium = t.h5,
    titleSmall = t.cardTitle,
    labelSmall = t.caption,
)

@Composable
fun GooGooseTheme(textSizePreset: TextSizePreset = TextSizePreset.STANDARD, content: @Composable () -> Unit) {
    val typography = typographyFor(textSizePreset)
    CompositionLocalProvider(LocalGooGooseTypography provides typography) {
        MaterialTheme(
            colorScheme = GooGooseDarkColorScheme,
            typography = materialTypography(typography),
            shapes = GooGooseShapes,
            content = content,
        )
    }
}
