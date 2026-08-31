package com.example.googoose.util

import org.junit.Assert.assertEquals
import org.junit.Test

class NumberFormatTest {

    @Test
    fun `formatMoney groups thousands and always shows two decimals`() {
        assertEquals("8,942.10", formatMoney(8942.10))
        assertEquals("45.00", formatMoney(45.0))
        assertEquals("0.00", formatMoney(0.0))
    }

    @Test
    fun `formatQty drops a trailing dot-zero but keeps real decimals`() {
        assertEquals("14", formatQty(14.0))
        assertEquals("11.5", formatQty(11.5))
        assertEquals("900", formatQty(900.0))
    }
}
