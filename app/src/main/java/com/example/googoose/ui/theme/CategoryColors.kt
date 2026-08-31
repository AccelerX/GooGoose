package com.example.googoose.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Categorical palette for the Reports category breakdown (bar list + pie
 * chart) — the dark-mode column of the dataviz skill's validated 8-hue
 * default, re-validated against GooGoose's actual dark surface (#232532):
 * `node scripts/validate_palette.js "<8 hex>" --mode dark --surface "#232532"`
 * passes all 8 on adjacent pairs, and separately the pie's wrap-around pair
 * (slot 8 next to slot 1) passes too. The full 8 do NOT clear the stricter
 * all-pairs check (a pie can put any two categories next to each other
 * depending on data), so colors are assigned by sorted rank (largest amount
 * = slot 1) rather than by category identity — that keeps every adjacency
 * actually on-screen (including the pie's wrap-around) within the validated
 * chain. Categories beyond the 8th (by amount) fold into a neutral "Other"
 * bucket rather than reusing a hue, per the skill's guidance.
 */
object CategoryColors {
    private val slots = listOf(
        Color(0xFF3987E5), // 1 blue
        Color(0xFFD95926), // 2 orange
        Color(0xFF199E70), // 3 aqua
        Color(0xFFC98500), // 4 yellow
        Color(0xFFD55181), // 5 magenta
        Color(0xFF008300), // 6 green
        Color(0xFF9085E9), // 7 violet
        Color(0xFFE66767), // 8 red
    )

    const val maxSlots = 8

    val other: Color = NocturneNeutral500

    /** [rank] is 0-based sorted position (0 = largest amount). Callers must cap at [maxSlots] and fold the rest into [other]. */
    fun forRank(rank: Int): Color = slots[rank % slots.size]
}
