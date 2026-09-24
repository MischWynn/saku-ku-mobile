package com.example.sakuku.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

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

    // Versi ringkas buat tempat sempit (kartu tenor, tombol nominal cepat): Rp750rb, Rp1,4jt, Rp1,2M.
    fun formatRupiahShort(amount: Double): String {
        fun oneDecimal(v: Double): String {
            val r = Math.round(v * 10) / 10.0
            return if (r == r.toLong().toDouble()) r.toLong().toString() else r.toString().replace('.', ',')
        }
        return when {
            amount >= 1_000_000_000 -> "Rp${oneDecimal(amount / 1_000_000_000)}M"
            amount >= 999_500 -> "Rp${oneDecimal(amount / 1_000_000)}jt"
            amount >= 1_000 -> "Rp${Math.round(amount / 1_000)}rb"
            else -> formatRupiah(amount)
        }
    }

    // Nomor referensi pendek buat ditampilin ke customer (Home/Notifikasi/Riwayat/Detail) -
    // UUID pengajuan penuh kepanjangan buat ditaro di kartu. Pola sama kayak staff web
    // dashboard (loan-queue-list.html, appId.slice(-8)) biar customer & staff bisa saling
    // cocokin nomor kalau perlu koordinasi manual.
    fun formatPengajuanRef(id: String): String = "#" + id.takeLast(8).uppercase()

    private val ID_LOCALE = Locale.Builder().setLanguage("id").setRegion("ID").build()
    private val DUE_DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy", ID_LOCALE)

    // tanggalPencairan + n bulan sampai lewatin hari ini - itu jatuh tempo cicilan "bulan ini".
    // Dipakai bareng oleh BayarViewModel (list lengkap) & HomeViewModel (preview 1 kartu) -
    // satu sumber kebenaran, gak diduplikasi. null kalau tanggalPencairan belum keisi backend
    // (data lama sebelum field ini ada, atau backend belum sempat restart).
    fun nextDueDateLabel(tanggalPencairanIso: String): String? =
        nextDueDate(tanggalPencairanIso)?.format(DUE_DATE_FORMATTER)

    // Jatuh tempo berikutnya = tanggal cair + n bulan, yang pertama gak sebelum hari ini.
    fun nextDueDate(tanggalPencairanIso: String, today: LocalDate = LocalDate.now()): LocalDate? {
        return try {
            val disbursedDate = LocalDateTime.parse(tanggalPencairanIso).toLocalDate()
            var due = disbursedDate.plusMonths(1)
            while (due.isBefore(today)) {
                due = due.plusMonths(1)
            }
            due
        } catch (e: DateTimeParseException) {
            null
        }
    }
}
