package com.example.googoose.ui.theme

import androidx.compose.ui.graphics.Color

// Nocturne design-system tokens, ported verbatim from
// _ds/nocturne-369a18ae-07a3-482c-ad23-c72860b6622d/styles.css — that file
// remains the source of truth if these values ever need to be retuned.
val NocturneBg = Color(0xFF161826)
val NocturneSurface = Color(0xFF232532)
val NocturneText = Color(0xFFE9E9ED)
val NocturneAccent = Color(0xFF9184D9)

val NocturneNeutral100 = Color(0xFFF3F5FE)
val NocturneNeutral200 = Color(0xFFE4E7F5)
val NocturneNeutral300 = Color(0xFFCFD3E5)
val NocturneNeutral400 = Color(0xFFB2B6CA)
val NocturneNeutral500 = Color(0xFF9397AB)
val NocturneNeutral600 = Color(0xFF75798C)
val NocturneNeutral700 = Color(0xFF595D6C)
val NocturneNeutral800 = Color(0xFF3F424D)
val NocturneNeutral900 = Color(0xFF292B31)

val NocturneAccent100 = Color(0xFFF5F4FF)
val NocturneAccent200 = Color(0xFFE7E5FE)
val NocturneAccent300 = Color(0xFFD2CEFD)
val NocturneAccent400 = Color(0xFFB5ABFC)
val NocturneAccent500 = Color(0xFF968AE0)
val NocturneAccent600 = Color(0xFF796CBF)
val NocturneAccent700 = Color(0xFF5D5294)
val NocturneAccent800 = Color(0xFF423A6A)
val NocturneAccent900 = Color(0xFF2B2741)

/** `color-mix(in srgb, var(--color-text) X%, transparent)` — text at reduced alpha. */
fun textAlpha(percent: Int): Color = NocturneText.copy(alpha = percent / 100f)

val NocturneDivider = textAlpha(16)
val NocturneTextMuted = textAlpha(55)
val NocturneTextMutedLight = textAlpha(70)

/**
 * Semantic aliases used directly by the custom (non-Material-default) styled
 * components — GooGoose's screens follow Nocturne's own component classes
 * (.btn/.card/.tag/.seg) rather than stock Material3 filled buttons, so most
 * composables read these instead of [androidx.compose.material3.MaterialTheme].
 */
object GooGooseColors {
    val background = NocturneBg
    val surface = NocturneSurface
    val text = NocturneText
    val textMuted = NocturneTextMuted
    val accent = NocturneAccent
    val divider = NocturneDivider

    val avatarBg = NocturneAccent800
    val avatarFg = NocturneAccent100
    val tagAccentBg = NocturneAccent800
    val tagAccentFg = NocturneAccent100
    val tagNeutralBg = NocturneNeutral800
    val tagNeutralFg = NocturneNeutral100

    val positiveBar = NocturneAccent
    val negativeBar = NocturneNeutral500
    val progressTrack = NocturneNeutral800

    val incomeAmount = NocturneAccent300

    /**
     * Not a Nocturne token — the design system has no error color (nothing in
     * the mockup needed one; every form was decorative). Added for the real
     * validation Phase 2 introduces (Add/Edit transaction). A standard
     * accessible dark-theme red, picked to sit comfortably against NocturneBg.
     */
    val error = Color(0xFFE5484D)
}
