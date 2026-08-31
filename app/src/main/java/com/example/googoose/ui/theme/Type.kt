package com.example.googoose.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.googoose.data.TextSizePreset

// styles.css uses Inter for --font-heading/--font-body with a system-ui
// fallback. Inter and the platform default sans-serif (Roboto) are close
// enough in metrics that v1 ships with FontFamily.Default rather than
// bundling font files or wiring the Google Fonts provider — swap this for a
// real Inter FontFamily later if pixel-exact typography matters.
private val HeadingFamily = FontFamily.Default
private val BodyFamily = FontFamily.Default
private val HeadingWeight = FontWeight.Medium // --font-heading-weight: 500

/**
 * One complete set of named text styles — the values behind [GooGooseType].
 * Weight/letterSpacing stay fixed across [TextSizePreset]s (that's styling,
 * not a legibility concern); only fontSize/lineHeight vary, and not
 * uniformly — compact/tight spots (bottom-nav labels, small kickers) scale a
 * lot less than body text and headings, so nothing overflows its layout at
 * the Large tier. Chart data-labels (e.g. the Reports 7-day bars) hardcode
 * their own `.copy(fontSize = ...)` on top of [caption]/[bodySmall] and so
 * are intentionally unaffected by any of this — see ReportsTab.
 */
data class GooGooseTypography(
    val h3: TextStyle,
    val h5: TextStyle,
    val dialogTitle: TextStyle,
    val balance: TextStyle,
    val headerBrand: TextStyle,
    val cardTitle: TextStyle,
    val cardKicker: TextStyle,
    val body: TextStyle,
    val bodySmall: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
    val tabLabel: TextStyle,
    val tag: TextStyle,
)

private val GooGooseTypographySmall = GooGooseTypography(
    h3 = TextStyle(fontFamily = HeadingFamily, fontWeight = HeadingWeight, fontSize = 22.sp, lineHeight = 25.sp),
    h5 = TextStyle(fontFamily = HeadingFamily, fontWeight = HeadingWeight, fontSize = 15.sp, lineHeight = 18.sp),
    dialogTitle = TextStyle(fontFamily = HeadingFamily, fontWeight = HeadingWeight, fontSize = 18.sp, lineHeight = 21.sp),
    balance = TextStyle(fontFamily = HeadingFamily, fontWeight = FontWeight.Medium, fontSize = 28.sp, letterSpacing = (-0.02).em),
    headerBrand = TextStyle(fontFamily = HeadingFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, letterSpacing = 0.01.em),
    cardTitle = TextStyle(fontFamily = HeadingFamily, fontWeight = HeadingWeight, fontSize = 15.sp, lineHeight = 18.sp),
    cardKicker = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 9.sp, letterSpacing = 0.1.em, textAlign = TextAlign.Start),
    body = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp),
    label = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp),
    caption = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 10.sp),
    tabLabel = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 8.sp),
    tag = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 10.sp, letterSpacing = 0.02.em),
)

private val GooGooseTypographyStandard = GooGooseTypography(
    h3 = TextStyle(fontFamily = HeadingFamily, fontWeight = HeadingWeight, fontSize = 25.sp, lineHeight = 28.sp),
    h5 = TextStyle(fontFamily = HeadingFamily, fontWeight = HeadingWeight, fontSize = 16.sp, lineHeight = 19.sp),
    dialogTitle = TextStyle(fontFamily = HeadingFamily, fontWeight = HeadingWeight, fontSize = 20.sp, lineHeight = 23.sp),
    balance = TextStyle(fontFamily = HeadingFamily, fontWeight = FontWeight.Medium, fontSize = 32.sp, letterSpacing = (-0.02).em),
    headerBrand = TextStyle(fontFamily = HeadingFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, letterSpacing = 0.01.em),
    cardTitle = TextStyle(fontFamily = HeadingFamily, fontWeight = HeadingWeight, fontSize = 17.sp, lineHeight = 20.sp),
    cardKicker = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 10.sp, letterSpacing = 0.1.em, textAlign = TextAlign.Start),
    body = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 23.sp),
    bodySmall = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    label = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    caption = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp),
    tabLabel = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 9.sp),
    tag = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp, letterSpacing = 0.02.em),
)

private val GooGooseTypographyLarge = GooGooseTypography(
    h3 = TextStyle(fontFamily = HeadingFamily, fontWeight = HeadingWeight, fontSize = 28.sp, lineHeight = 32.sp),
    h5 = TextStyle(fontFamily = HeadingFamily, fontWeight = HeadingWeight, fontSize = 18.sp, lineHeight = 21.sp),
    dialogTitle = TextStyle(fontFamily = HeadingFamily, fontWeight = HeadingWeight, fontSize = 22.sp, lineHeight = 26.sp),
    balance = TextStyle(fontFamily = HeadingFamily, fontWeight = FontWeight.Medium, fontSize = 36.sp, letterSpacing = (-0.02).em),
    headerBrand = TextStyle(fontFamily = HeadingFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, letterSpacing = 0.01.em),
    cardTitle = TextStyle(fontFamily = HeadingFamily, fontWeight = HeadingWeight, fontSize = 19.sp, lineHeight = 23.sp),
    cardKicker = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp, letterSpacing = 0.1.em, textAlign = TextAlign.Start),
    body = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 25.sp),
    bodySmall = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 23.sp),
    label = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp),
    caption = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    tabLabel = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 10.sp),
    tag = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, letterSpacing = 0.02.em),
)

fun typographyFor(preset: TextSizePreset): GooGooseTypography = when (preset) {
    TextSizePreset.SMALL -> GooGooseTypographySmall
    TextSizePreset.STANDARD -> GooGooseTypographyStandard
    TextSizePreset.LARGE -> GooGooseTypographyLarge
}

/** Provided by [GooGooseTheme] from the user's Settings choice; defaults to Standard where nothing provides it (e.g. @Preview). */
val LocalGooGooseTypography = staticCompositionLocalOf { GooGooseTypographyStandard }

/**
 * Named text styles mirroring the CSS classes Till.dc.html actually uses —
 * unchanged call syntax (`GooGooseType.cardTitle` etc.) for every existing
 * usage site; each property now resolves through [LocalGooGooseTypography]
 * the same way `MaterialTheme.typography` resolves through Material3's own
 * CompositionLocal.
 */
object GooGooseType {
    val h3: TextStyle @Composable get() = LocalGooGooseTypography.current.h3
    val h5: TextStyle @Composable get() = LocalGooGooseTypography.current.h5
    val dialogTitle: TextStyle @Composable get() = LocalGooGooseTypography.current.dialogTitle
    val balance: TextStyle @Composable get() = LocalGooGooseTypography.current.balance
    val headerBrand: TextStyle @Composable get() = LocalGooGooseTypography.current.headerBrand
    val cardTitle: TextStyle @Composable get() = LocalGooGooseTypography.current.cardTitle
    val cardKicker: TextStyle @Composable get() = LocalGooGooseTypography.current.cardKicker
    val body: TextStyle @Composable get() = LocalGooGooseTypography.current.body
    val bodySmall: TextStyle @Composable get() = LocalGooGooseTypography.current.bodySmall
    val label: TextStyle @Composable get() = LocalGooGooseTypography.current.label
    val caption: TextStyle @Composable get() = LocalGooGooseTypography.current.caption
    val tabLabel: TextStyle @Composable get() = LocalGooGooseTypography.current.tabLabel
    val tag: TextStyle @Composable get() = LocalGooGooseTypography.current.tag
}
