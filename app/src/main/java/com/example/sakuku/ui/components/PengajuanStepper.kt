package com.example.sakuku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.PlusJakartaSans

// Langkah proses pengajuan, dipakai bareng di Beranda (kartu pengajuan aktif), Detail Status
// Pengajuan, dan Detail Pencairan - biar urutannya selalu sama di mana pun.
private val STEPS = listOf("Terkirim", "Marketing", "BM", "Back Office", "Cair")
private val RejectRed = Color(0xFFF28FA0)

private data class StepperState(val current: Int, val rejected: Boolean, val cancelled: Boolean)

// current = langkah yang lagi berjalan. "Terkirim" selalu selesai begitu pengajuan ada.
private fun stepperStateOf(status: String): StepperState = when (status) {
    "MARKETING_REVIEW" -> StepperState(1, rejected = false, cancelled = false)
    "BM_REVIEW" -> StepperState(2, rejected = false, cancelled = false)
    "BACKOFFICE_REVIEW" -> StepperState(3, rejected = false, cancelled = false)
    // Cair = semua langkah selesai (current di luar range, jadi semuanya ke-render "lewat").
    "DISBURSED" -> StepperState(STEPS.size, rejected = false, cancelled = false)
    "MARKETING_REJECTED" -> StepperState(1, rejected = true, cancelled = false)
    "BM_REJECTED" -> StepperState(2, rejected = true, cancelled = false)
    "CANCELLED" -> StepperState(1, rejected = false, cancelled = true)
    else -> StepperState(1, rejected = false, cancelled = false)
}

@Composable
fun PengajuanStepper(status: String, modifier: Modifier = Modifier) {
    val state = stepperStateOf(status)
    // Ditolak -> langkah yang nolak jadi merah. Dibatalkan -> abu (gak ada yang "gagal").
    val stopColor = when {
        state.rejected -> RejectRed
        state.cancelled -> Color.White.copy(alpha = 0.35f)
        else -> BlobDark
    }
    val stopped = state.rejected || state.cancelled

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            STEPS.indices.forEach { i ->
                val passed = i < state.current
                val isCurrent = i == state.current
                Box(
                    modifier = Modifier
                        .size(if (isCurrent) 14.dp else 10.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                passed -> BlobDark
                                isCurrent && stopped -> stopColor
                                isCurrent -> BlobDark.copy(alpha = 0.25f)
                                else -> Color.White.copy(alpha = 0.18f)
                            }
                        )
                        .border(if (isCurrent) 2.dp else 0.dp, stopColor, CircleShape)
                )
                if (i < STEPS.lastIndex) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(if (i < state.current) BlobDark else Color.White.copy(alpha = 0.15f))
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            STEPS.forEachIndexed { i, label ->
                val isCurrent = i == state.current
                Text(
                    text = when {
                        isCurrent && state.rejected -> "Ditolak"
                        isCurrent && state.cancelled -> "Batal"
                        else -> label
                    },
                    color = when {
                        isCurrent && stopped -> stopColor
                        isCurrent -> Color.White
                        i < state.current -> Color.White.copy(alpha = 0.75f)
                        else -> Color.White.copy(alpha = 0.4f)
                    },
                    fontFamily = PlusJakartaSans,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 10.sp,
                    textAlign = when (i) {
                        0 -> TextAlign.Start
                        STEPS.lastIndex -> TextAlign.End
                        else -> TextAlign.Center
                    }
                )
            }
        }
    }
}

