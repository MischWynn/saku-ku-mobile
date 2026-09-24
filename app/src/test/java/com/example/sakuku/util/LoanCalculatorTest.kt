package com.example.sakuku.util

import org.junit.Assert.assertEquals
import org.junit.Test

class LoanCalculatorTest {

    @Test
    fun formatRupiahShort_ribuan() {
        assertEquals("Rp750rb", LoanCalculator.formatRupiahShort(750_000.0))
    }

    @Test
    fun formatRupiahShort_jutaanBulatTanpaKoma() {
        assertEquals("Rp5jt", LoanCalculator.formatRupiahShort(5_000_000.0))
    }

    @Test
    fun formatRupiahShort_jutaanSatuDesimal() {
        assertEquals("Rp1,4jt", LoanCalculator.formatRupiahShort(1_373_333.0))
    }

    @Test
    fun formatRupiahShort_hampirSejutaJadiJuta_bukan1000rb() {
        assertEquals("Rp1jt", LoanCalculator.formatRupiahShort(999_600.0))
    }

    @Test
    fun estimate_flatRate_pakaiPersenMentah() {
        // 8jt, 6 bulan, bunga 3% (angka persen mentah dari API) -> (8jt + 240rb) / 6
        val e = LoanCalculator.estimate(8_000_000.0, 6, 3.0)
        assertEquals(240_000.0, e.totalBunga, 0.01)
        assertEquals(1_373_333.33, e.cicilanBulanan, 0.01)
    }
}
