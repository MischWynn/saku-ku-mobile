package com.example.sakuku.ui.screens.riwayat

import androidx.compose.ui.graphics.Color

// Diekstrak dari RiwayatScreen.kt (dulu private di situ) biar bisa dipakai bareng sama layar
// detail/timeline Status Pinjaman - satu sumber kebenaran buat label/warna status, bukan
// diduplikasi ulang di 2 tempat (pelajaran berulang di project ini soal field/label drift).

internal fun statusLabel(status: String): String = when (status) {
    "MARKETING_REVIEW" -> "Direview Marketing"
    "MARKETING_REJECTED" -> "Ditolak Marketing"
    "BM_REVIEW" -> "Direview BM"
    "BM_REJECTED" -> "Ditolak BM"
    "BACKOFFICE_REVIEW" -> "Proses Pencairan"
    "DISBURSED" -> "Dana Cair"
    "CANCELLED" -> "Dibatalkan"
    else -> status
}

internal fun statusColor(status: String): Color = when (status) {
    "MARKETING_REVIEW", "BM_REVIEW" -> Color(0xFFF2C25F)
    "BACKOFFICE_REVIEW" -> Color(0xFF7DB4F5)
    "DISBURSED" -> Color(0xFF5FE3AB)
    "MARKETING_REJECTED", "BM_REJECTED" -> Color(0xFFF28FA0)
    else -> Color.White.copy(alpha = 0.5f)
}

internal fun isRejectedStatus(status: String): Boolean =
    status == "MARKETING_REJECTED" || status == "BM_REJECTED"

internal fun tujuanLabel(apiValue: String): String = when (apiValue) {
    "MODAL_USAHA" -> "Modal Usaha"
    "KONSUMTIF" -> "Konsumtif"
    "PENDIDIKAN" -> "Pendidikan"
    "KESEHATAN" -> "Kesehatan"
    "RENOVASI" -> "Renovasi"
    "LAINNYA" -> "Lainnya"
    else -> apiValue
}

internal fun formatTanggal(iso: String?): String {
    if (iso.isNullOrBlank()) return "-"
    return iso.take(10) // "yyyy-MM-dd" dari string ISO, cukup buat tampilan ringkas
}

// roleName mentah dari backend ("MARKETING"/"BM"/"BACK_OFFICE") -> label rapi buat timeline.
internal fun roleDisplayName(roleName: String?): String = when (roleName) {
    "MARKETING" -> "Marketing"
    "BM" -> "Branch Manager"
    "BACK_OFFICE" -> "Back Office"
    else -> roleName ?: "Sistem"
}

// action mentah backend ("APPROVE"/"REJECT"/"DISBURSE") -> kalimat timeline yang enak dibaca.
internal fun historyActionLabel(action: String, roleName: String?): String {
    val role = roleDisplayName(roleName)
    return when (action) {
        "APPROVE" -> "Disetujui oleh $role"
        "REJECT" -> "Ditolak oleh $role"
        "DISBURSE" -> "Dana dicairkan oleh $role"
        else -> "$action oleh $role"
    }
}
