package com.example.googoose.util

import com.example.googoose.data.Language
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Transactions now carry real epoch-millis timestamps instead of the mockup's
 * pre-baked display strings, so grouping/time labels are computed here at
 * render time. No java.time — minSdk 24 would need core-library desugaring
 * for it, and Calendar/SimpleDateFormat is plenty for this app's needs.
 *
 * Display functions take [Language] and hand-format the Chinese case rather
 * than delegating to `SimpleDateFormat(..., Locale.CHINA)`: this app wants
 * specific compact conventions (single-character weekdays, no ICU-dependent
 * verbosity) rather than whatever the platform's Chinese locale tables give.
 */
object DateFormat {

    private const val ONE_DAY_MILLIS = 24L * 60 * 60 * 1000

    /** Calendar.DAY_OF_WEEK is 1=Sunday..7=Saturday — this array is index-aligned to that. */
    private val zhWeekday = arrayOf("日", "一", "二", "三", "四", "五", "六")

    private fun startOfDay(millis: Long): Long = calendarOf(millis).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun calendarOf(millis: Long): Calendar = Calendar.getInstance().apply { timeInMillis = millis }

    /** "Today" / "Yesterday" / "Thu, Aug 27" (or "今天" / "昨天" / "8月27日 周四") — the Transactions-tab group header. */
    fun dateGroupLabel(occurredAt: Long, now: Long = System.currentTimeMillis(), language: Language = Language.EN): String {
        val dayStart = startOfDay(occurredAt)
        val todayStart = startOfDay(now)
        val dayDiff = ((todayStart - dayStart) / ONE_DAY_MILLIS).toInt()
        return when (dayDiff) {
            0 -> if (language == Language.ZH_CN) "今天" else "Today"
            1 -> if (language == Language.ZH_CN) "昨天" else "Yesterday"
            else -> if (language == Language.ZH_CN) {
                val cal = calendarOf(occurredAt)
                "${cal.get(Calendar.MONTH) + 1}月${cal.get(Calendar.DAY_OF_MONTH)}日 周${zhWeekday[cal.get(Calendar.DAY_OF_WEEK) - 1]}"
            } else {
                SimpleDateFormat("EEE, MMM d", Locale.US).format(Date(occurredAt))
            }
        }
    }

    /** "5:42 PM" or "下午5:42" */
    fun timeOfDay(millis: Long, language: Language = Language.EN): String {
        if (language != Language.ZH_CN) return SimpleDateFormat("h:mm a", Locale.US).format(Date(millis))
        val cal = calendarOf(millis)
        val hour24 = cal.get(Calendar.HOUR_OF_DAY)
        val hour12 = cal.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        val minute = cal.get(Calendar.MINUTE).toString().padStart(2, '0')
        val period = if (hour24 < 12) "上午" else "下午"
        return "$period$hour12:$minute"
    }

    /** "Aug 30, 2026 · 5:42 PM" or "2026年8月30日 · 下午5:42" — used for the read-only Created/Last modified rows. */
    fun dateTime(millis: Long, language: Language = Language.EN): String {
        val datePart = if (language == Language.ZH_CN) {
            val cal = calendarOf(millis)
            "${cal.get(Calendar.YEAR)}年${cal.get(Calendar.MONTH) + 1}月${cal.get(Calendar.DAY_OF_MONTH)}日"
        } else {
            SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(millis))
        }
        return "$datePart · ${timeOfDay(millis, language)}"
    }

    /** Single-character weekday: "M"/"T"/"W"... (repeats — Tue/Thu both "T", Sat/Sun both "S") or "一"/"二"/... (all unique) for the Reports 7-day chart. */
    fun weekdayInitial(millis: Long, language: Language = Language.EN): String {
        if (language == Language.ZH_CN) return zhWeekday[calendarOf(millis).get(Calendar.DAY_OF_WEEK) - 1]
        return SimpleDateFormat("EEE", Locale.US).format(Date(millis)).take(1)
    }

    /** "Aug 28" or "8月28日" — unambiguous (unlike [weekdayInitial]), used in the low-cash-flow warning. */
    fun shortDate(millis: Long, language: Language = Language.EN): String {
        if (language == Language.ZH_CN) {
            val cal = calendarOf(millis)
            return "${cal.get(Calendar.MONTH) + 1}月${cal.get(Calendar.DAY_OF_MONTH)}日"
        }
        return SimpleDateFormat("MMM d", Locale.US).format(Date(millis))
    }

    /** Epoch millis for a given local date at the given time-of-day (used to seed demo data). */
    fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            set(year, month - 1, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    /** "2026-08-30" — for the Add-transaction date field's display text. Kept locale-independent (compact, form-field style). */
    fun isoDate(millis: Long): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))

    /**
     * Compose Material3's DatePicker returns UTC-midnight millis for the
     * selected calendar date — naively storing that as a local timestamp
     * shifts the date back a day in negative-UTC-offset timezones. This
     * re-anchors the picked Y/M/D in the local timezone at the current
     * time-of-day (a new transaction's date can be backdated, but its time
     * defaults to "now", same as when you'd jot it down on paper).
     */
    fun fromDatePickerSelection(utcMidnightMillis: Long, now: Long = System.currentTimeMillis()): Long {
        val picked = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMidnightMillis }
        val nowLocal = Calendar.getInstance().apply { timeInMillis = now }
        return Calendar.getInstance().apply {
            set(
                picked.get(Calendar.YEAR),
                picked.get(Calendar.MONTH),
                picked.get(Calendar.DAY_OF_MONTH),
                nowLocal.get(Calendar.HOUR_OF_DAY),
                nowLocal.get(Calendar.MINUTE),
                nowLocal.get(Calendar.SECOND),
            )
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Local midnight (00:00:00) for the calendar date encoded in a DatePicker
     * selection — used for the "before this date" transactions filter, which
     * needs the day boundary itself rather than "now"'s time of day.
     */
    fun localDayStart(utcMidnightMillis: Long): Long {
        val picked = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMidnightMillis }
        return Calendar.getInstance().apply {
            set(picked.get(Calendar.YEAR), picked.get(Calendar.MONTH), picked.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
