package com.example.googoose.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val moneyFormat = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))

/**
 * Always-2-decimals, thousands-grouped money formatting (no currency symbol —
 * callers prepend "$"/"+$"/"-$" themselves). Real transaction totals can now
 * exceed 999, unlike Phase 1's static sample amounts, so grouping matters.
 */
fun formatMoney(amount: Double): String = moneyFormat.format(amount)

/**
 * Loose JS-`String(number)`-style formatting for stock quantities/deltas: no
 * fixed decimal count, integral values print without a trailing ".0".
 */
fun formatQty(value: Double): String {
    if (value == value.toLong().toDouble()) return value.toLong().toString()
    return value.toString().trimEnd('0').trimEnd('.')
}

/** Symbol for the Settings currency codes — matches [com.example.googoose.ui.settings.currencyOptions]. */
fun currencySymbol(code: String): String = when (code) {
    "EUR" -> "€"
    "GBP" -> "£"
    "CNY" -> "¥"
    else -> "$" // USD, CAD, and any unrecognized code
}
