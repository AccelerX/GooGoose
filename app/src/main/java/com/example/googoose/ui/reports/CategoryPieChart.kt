package com.example.googoose.ui.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.example.googoose.viewmodel.GooGooseLogic

/**
 * Pie chart via `Canvas.drawArc`, one slice per [rows] entry (already
 * rank-colored and Other-folded by [GooGooseLogic.categoryBreakdown]). Each
 * slice carries a small surface-colored gap on both sides — the dataviz
 * skill's "2px gap between fills" mark spec — as a secondary encoding
 * alongside color, and because the bar list beside this chart already
 * serves as the legend (same rows, same colors), this stays unlabeled.
 */
@Composable
fun CategoryPieChart(rows: List<GooGooseLogic.CategoryBreakdownRow>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(150.dp)) {
        if (rows.isEmpty()) return@Canvas
        val gapDegrees = 2f
        var startAngle = -90f
        val diameter = size.minDimension
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        rows.forEach { row ->
            val sweep = row.pieFraction * 360f
            val drawSweep = (sweep - gapDegrees).coerceAtLeast(0f)
            drawArc(
                color = row.color,
                startAngle = startAngle + gapDegrees / 2f,
                sweepAngle = drawSweep,
                useCenter = true,
                topLeft = topLeft,
                size = arcSize,
            )
            startAngle += sweep
        }
    }
}
