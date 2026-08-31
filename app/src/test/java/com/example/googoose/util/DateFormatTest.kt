package com.example.googoose.util

import com.example.googoose.data.Language
import org.junit.Assert.assertEquals
import org.junit.Test

class DateFormatTest {

    @Test
    fun `isoDate formats as yyyy-MM-dd`() {
        assertEquals("2026-08-30", DateFormat.isoDate(DateFormat.at(2026, 8, 30, 0, 0)))
    }

    @Test
    fun `timeOfDay formats 12-hour with AM PM`() {
        assertEquals("5:42 PM", DateFormat.timeOfDay(DateFormat.at(2026, 8, 30, 17, 42)))
        assertEquals("9:15 AM", DateFormat.timeOfDay(DateFormat.at(2026, 8, 30, 9, 15)))
    }

    @Test
    fun `dateGroupLabel falls back to weekday, month, day beyond yesterday`() {
        val now = DateFormat.at(2026, 8, 30, 12, 0)
        assertEquals("Today", DateFormat.dateGroupLabel(DateFormat.at(2026, 8, 30, 9, 0), now))
        assertEquals("Yesterday", DateFormat.dateGroupLabel(DateFormat.at(2026, 8, 29, 9, 0), now))
        assertEquals("Thu, Aug 27", DateFormat.dateGroupLabel(DateFormat.at(2026, 8, 27, 9, 0), now))
    }

    // ---- Chinese formatting ----

    @Test
    fun `timeOfDay in Chinese uses 上午 下午 prefix, no colon-space`() {
        assertEquals("下午5:42", DateFormat.timeOfDay(DateFormat.at(2026, 8, 30, 17, 42), Language.ZH_CN))
        assertEquals("上午9:15", DateFormat.timeOfDay(DateFormat.at(2026, 8, 30, 9, 15), Language.ZH_CN))
        assertEquals("上午12:00", DateFormat.timeOfDay(DateFormat.at(2026, 8, 30, 0, 0), Language.ZH_CN)) // midnight
        assertEquals("下午12:00", DateFormat.timeOfDay(DateFormat.at(2026, 8, 30, 12, 0), Language.ZH_CN)) // noon
    }

    @Test
    fun `dateGroupLabel in Chinese uses 今天 昨天 and month-day-weekday order`() {
        val now = DateFormat.at(2026, 8, 30, 12, 0)
        assertEquals("今天", DateFormat.dateGroupLabel(DateFormat.at(2026, 8, 30, 9, 0), now, Language.ZH_CN))
        assertEquals("昨天", DateFormat.dateGroupLabel(DateFormat.at(2026, 8, 29, 9, 0), now, Language.ZH_CN))
        assertEquals("8月27日 周四", DateFormat.dateGroupLabel(DateFormat.at(2026, 8, 27, 9, 0), now, Language.ZH_CN))
    }

    @Test
    fun `weekdayInitial in Chinese is single-character and never repeats across a week`() {
        val week = (24..30).map { day -> DateFormat.weekdayInitial(DateFormat.at(2026, 8, day, 12, 0), Language.ZH_CN) }
        assertEquals(listOf("一", "二", "三", "四", "五", "六", "日"), week)
        assertEquals(7, week.toSet().size) // unlike English M/T/W/T/F/S/S, no duplicates
    }

    @Test
    fun `shortDate and dateTime in Chinese use year-month-day with 年月日`() {
        assertEquals("8月28日", DateFormat.shortDate(DateFormat.at(2026, 8, 28, 0, 0), Language.ZH_CN))
        assertEquals(
            "2026年8月30日 · 下午5:42",
            DateFormat.dateTime(DateFormat.at(2026, 8, 30, 17, 42), Language.ZH_CN),
        )
    }
}
