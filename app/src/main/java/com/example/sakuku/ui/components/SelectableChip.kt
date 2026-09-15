package com.example.sakuku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.PlusJakartaSans

private val ChipBorder = Color.White.copy(alpha = 0.12f)

// Dipakai bareng buat pilihan Durasi Peminjaman (label+subtitle) dan Tujuan Pinjaman
// (label doang) - dua-duanya row horizontal-scroll, biar konsisten kalau opsinya nambah
// gak perlu redesign ulang tiap tempat (Home Simulasi, Ajukan Pinjaman step 1).
@Composable
fun SelectableChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    val shape = RoundedCornerShape(if (subtitle != null) 14.dp else 999.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(if (isSelected) BlobDark else Color.White.copy(alpha = 0.05f))
            .border(1.dp, if (isSelected) Color.Transparent else ChipBorder, shape)
            .clickable(onClick = onClick)
            .padding(
                horizontal = if (subtitle != null) 16.dp else 14.dp,
                vertical = if (subtitle != null) 10.dp else 9.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = if (isSelected) Color(0xFF04160F) else Color.White,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        subtitle?.let {
            Text(
                text = it,
                color = if (isSelected) Color(0xFF04160F).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.45f),
                fontFamily = PlusJakartaSans,
                fontSize = 9.5.sp
            )
        }
    }
}
