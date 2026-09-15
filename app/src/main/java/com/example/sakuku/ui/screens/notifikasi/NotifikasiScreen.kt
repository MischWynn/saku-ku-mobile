package com.example.sakuku.ui.screens.notifikasi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sakuku.data.remote.dto.NotificationResponse
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.sakukuBlobBackground
import com.example.sakuku.ui.theme.SakukuTheme

private val GlassFill = Color.White.copy(alpha = 0.05f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)
private val UnreadFill = BlobDark.copy(alpha = 0.09f)
private val UnreadBorder = BlobDark.copy(alpha = 0.22f)
private val AccentCyan = Color(0xFF22D3EE)
private val Rose = Color(0xFFF28FA0)
private val Emerald = Color(0xFF5FE3AB)

@Composable
fun NotifikasiScreen(viewModel: NotifikasiViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    NotifikasiScreenContent(uiState = uiState, onRetry = viewModel::load, onItemClick = viewModel::markAsRead)
}

@Composable
private fun NotifikasiScreenContent(
    uiState: NotifikasiUiState,
    onRetry: () -> Unit,
    onItemClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .sakukuBlobBackground()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Notifikasi",
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.weight(1f)
                )
                if (uiState.unreadCount > 0) {
                    Text(
                        text = "${uiState.unreadCount} belum dibaca",
                        color = Color(0xFF04160F),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(BlobDark)
                            .padding(horizontal = 11.dp, vertical = 5.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BlobDark)
                    }
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(GlassFill)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(uiState.errorMessage, color = Color.White.copy(alpha = 0.8f), fontFamily = PlusJakartaSans, fontSize = 12.5.sp)
                        Text(
                            "Coba lagi",
                            color = BlobDark,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            modifier = Modifier.clickable(onClick = onRetry)
                        )
                    }
                }
                uiState.items.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Belum ada notifikasi",
                            color = Color.White.copy(alpha = 0.5f),
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp
                        )
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.items) { item ->
                            NotifikasiCard(item, onClick = { onItemClick(item.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotifikasiCard(item: NotificationResponse, onClick: () -> Unit) {
    val (icon, iconColor) = notifIconFor(item.judul)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (!item.isRead) UnreadFill else GlassFill)
            .border(1.dp, if (!item.isRead) UnreadBorder else GlassBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(
                    text = item.judul,
                    color = if (!item.isRead) Color.White else Color.White.copy(alpha = 0.6f),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                if (!item.isRead) {
                    Box(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(AccentCyan)
                    )
                }
            }
            Text(
                text = item.pesan,
                color = Color.White.copy(alpha = 0.55f),
                fontFamily = PlusJakartaSans,
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )
            Text(
                text = formatTanggal(item.createdAt),
                color = Color.White.copy(alpha = 0.35f),
                fontFamily = PlusJakartaSans,
                fontSize = 10.sp
            )
        }
    }
}

private fun notifIconFor(judul: String): Pair<ImageVector, Color> = when {
    judul.contains("Ditolak", ignoreCase = true) -> Icons.Rounded.Cancel to Rose
    judul.contains("cair", ignoreCase = true) -> Icons.Rounded.Payments to Emerald
    else -> Icons.Rounded.CheckCircle to Emerald
}

private fun formatTanggal(iso: String?): String {
    if (iso.isNullOrBlank()) return "-"
    return iso.take(10)
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun NotifikasiScreenPreview() {
    SakukuTheme {
        NotifikasiScreenContent(
            uiState = NotifikasiUiState(
                isLoading = false,
                items = listOf(
                    NotificationResponse("1", "Pengajuan Disetujui oleh Branch Manager", "Pengajuan Anda telah disetujui oleh BM dan diteruskan ke Back Office untuk diproses pencairan.", false, "2026-09-13T10:00:00"),
                    NotificationResponse("2", "Dana Pengajuan berhasil dicairkan!", "Pengajuan Anda telah dicairkan oleh Back Office. Silakan cek rekening Anda.", false, "2026-09-12T09:00:00"),
                    NotificationResponse("3", "Pengajuan Ditolak oleh Branch Manager", "Pengajuan anda telah ditolak. Silakan periksa catatan untuk informasi lebih lanjut.", true, "2026-09-10T09:00:00")
                )
            ),
            onRetry = {},
            onItemClick = {}
        )
    }
}
