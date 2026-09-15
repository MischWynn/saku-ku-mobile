package com.example.sakuku.util

// Formula flat-rate resmi (sama persis dipakai web dashboard & drawer review staff, lihat
// CLAUDE.md project - JANGAN diganti ke reducing-balance tanpa nyamain juga sisi backend/FE lain):
//   total_bunga = nominal * (interestRate / 100)
//   cicilan_bulanan = (nominal + total_bunga) / tenor
// PENTING: interestRate dari GET /bunga-tenor itu ANGKA PERSEN MENTAH (mis. 3.0 buat "3%"),
// BUKAN pecahan 0-1 (bukan 0.03) - dicross-check langsung ke response API asli 13 Sept,
// sempet salah asumsi 0.03 di draft awal sebelum diverifikasi. Angular web frontend
// (pengajuan-api.model.ts, calculateEstInstallment) pakai pembagian /100 yang sama.
data class LoanEstimate(
    val totalBunga: Double,
    val totalPembayaran: Double,
    val cicilanBulanan: Double
)

object LoanCalculator {
    fun estimate(nominal: Double, tenor: Int, interestRate: Double): LoanEstimate {
        if (tenor <= 0) return LoanEstimate(0.0, nominal, nominal)
        val totalBunga = nominal * (interestRate / 100.0)
        val totalPembayaran = nominal + totalBunga
        val cicilanBulanan = totalPembayaran / tenor
        return LoanEstimate(totalBunga, totalPembayaran, cicilanBulanan)
    }

    fun formatRupiah(amount: Double): String {
        val rounded = Math.round(amount)
        val grouped = rounded.toString().reversed().chunked(3).joinToString(".").reversed()
        return "Rp$grouped"
    }
}
