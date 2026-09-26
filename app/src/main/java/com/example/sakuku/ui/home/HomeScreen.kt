@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.sakuku.ui.home

import com.example.sakuku.ui.components.PengajuanStepper
import androidx.compose.material.icons.rounded.NotificationsNone
import java.time.LocalTime
import com.example.sakuku.ui.theme.tierStyleOf
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Animatable
import com.example.sakuku.ui.theme.screenTitleInset
import com.example.sakuku.ui.theme.ScreenPadding
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material.icons.rounded.PriceChange
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sakuku.R
import com.example.sakuku.data.remote.dto.BungaTenorResponse
import com.example.sakuku.data.remote.dto.PengajuanMeResponse
import com.example.sakuku.data.remote.dto.PlafondResponse
import com.example.sakuku.ui.components.EditableNominalField
import com.example.sakuku.ui.components.GradientButton
import com.example.sakuku.ui.components.PengajuanButtonGradient
import com.example.sakuku.ui.components.RoundSliderThumb
import com.example.sakuku.ui.components.SelectableChip
import com.example.sakuku.ui.theme.BgBottom
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.BlobLight
import com.example.sakuku.ui.theme.BlobMid
import com.example.sakuku.ui.theme.ButtonTurquoiseDeep
import com.example.sakuku.ui.theme.ButtonTurquoiseLight
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.sakukuBlobBackground
import com.example.sakuku.ui.theme.SakukuTheme
import com.example.sakuku.ui.theme.Teal900
import com.example.sakuku.util.LoanCalculator
import kotlinx.coroutines.launch
import androidx.annotation.DrawableRes
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.IntrinsicSize
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val GlassFill = Color.White.copy(alpha = 0.05f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)

private fun tierColor(namaPlafond: String): Color = when (namaPlafond.lowercase()) {
    "bronze" -> Color(0xFFC98A4B)
    "silver" -> Color(0xFFAEBCC9)
    "gold" -> Color(0xFFE7C468)
    "platinum" -> Color(0xFF8FD4E6)
    else -> Color.White.copy(alpha = 0.6f)
}

@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onNavigateToPlafond: () -> Unit = {},
    onNavigateToSimulasi: () -> Unit = {},
    onNavigateToApply: () -> Unit = {},
    onNavigateToRiwayat: () -> Unit = {},
    onNavigateToRekening: () -> Unit = {},
    onNavigateToBayar: () -> Unit = {},
    onNavigateToBantuan: () -> Unit = {},
    onNavigateToStatusDetail: (String) -> Unit = {},
    onNavigateToKtpDataDiri: () -> Unit = {},
    onNavigateToDataPekerjaan: () -> Unit = {},
    onNavigateToNotifikasi: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.refresh() }
    HomeScreenContent(
        uiState = uiState,
        onNominalChange = viewModel::onNominalChange,
        onTenorSelect = viewModel::onTenorSelect,
        onRetry = viewModel::retry,
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToRegister = onNavigateToRegister,
        onNavigateToPlafond = onNavigateToPlafond,
        onNavigateToSimulasi = onNavigateToSimulasi,
        onNavigateToApply = onNavigateToApply,
        onNavigateToRiwayat = onNavigateToRiwayat,
        onNavigateToRekening = onNavigateToRekening,
        onNavigateToBayar = onNavigateToBayar,
        onNavigateToBantuan = onNavigateToBantuan,
        onNavigateToStatusDetail = onNavigateToStatusDetail,
        onNavigateToKtpDataDiri = onNavigateToKtpDataDiri,
        onNavigateToDataPekerjaan = onNavigateToDataPekerjaan,
        onNavigateToNotifikasi = onNavigateToNotifikasi
    )
}

@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onNominalChange: (Double) -> Unit,
    onTenorSelect: (String) -> Unit,
    onRetry: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit = {},
    onNavigateToPlafond: () -> Unit,
    onNavigateToApply: () -> Unit,
    onNavigateToRiwayat: () -> Unit = {},
    onNavigateToRekening: () -> Unit = {},
    onNavigateToBayar: () -> Unit = {},
    onNavigateToBantuan: () -> Unit = {},
    onNavigateToSimulasi: () -> Unit = {},
    onNavigateToStatusDetail: (String) -> Unit = {},
    onNavigateToKtpDataDiri: () -> Unit = {},
    onNavigateToDataPekerjaan: () -> Unit = {},
    onNavigateToNotifikasi: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .sakukuBlobBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding.Horizontal)
                .padding(top = 24.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            TopBar(
                onNavigateToLogin = onNavigateToLogin,
                customerName = uiState.customerName,
                unreadNotifCount = uiState.unreadNotifCount,
                onBellClick = onNavigateToNotifikasi
            )

            if (!uiState.isLoggedIn) {
                HeroBannerSection(onNavigateToLogin = onNavigateToLogin, isLoggedIn = uiState.isLoggedIn)
            }


            if (uiState.isLoggedIn) {
                PromoBannerSection()
            }

            QuickAccessSection(
                isLoggedIn = uiState.isLoggedIn,
                onDaftarMasuk = onNavigateToLogin,
                onDaftar = onNavigateToRegister,
                onSimulasi = onNavigateToSimulasi,
                onCekPlafond = onNavigateToPlafond,
                onAjukan = onNavigateToApply,
                onRiwayat = onNavigateToRiwayat,
                onRekening = onNavigateToRekening,
                onCaraBayar = onNavigateToBayar,
                onBantuan = onNavigateToBantuan
            )

            if (!uiState.isLoggedIn) {
                BenefitsSection()
                PlafondPreviewSection(tiers = uiState.tiers, isLoading = uiState.isLoading)
                ApplicationFlowSection(maxNominal = uiState.maxNominal)

//                SimulasiSection(
//                    modifier = Modifier.bringIntoViewRequester(simulasiAnchor),
//                    uiState = uiState,
//                    onNominalChange = onNominalChange,
//                    onTenorSelect = onTenorSelect,
//                    onRetry = onRetry,
//                    onNavigateToLogin = onNavigateToLogin
//                )
            }

            if (uiState.isLoggedIn) {
                PlafondOrLoanCards(
                    uiState = uiState,
                    onAjukan = onNavigateToApply,
                    onLihatStatus = { uiState.activeLoan?.let { onNavigateToStatusDetail(it.id) } },
                    onBayarSekarang = onNavigateToBayar,
                    onLengkapiProfil = { req ->
                        when (req) {
                            ProfileRequirement.KTP,
                            ProfileRequirement.TANGGAL_LAHIR -> onNavigateToKtpDataDiri()
                            ProfileRequirement.PEKERJAAN -> onNavigateToDataPekerjaan()
                            ProfileRequirement.REKENING -> onNavigateToRekening()
                        }
                    }
                )
                PlafondPreviewSection(tiers = uiState.tiers, isLoading = uiState.isLoading)
            }

            TrustFooterSection()
        }
    }
}

@Composable
private fun PlafondOrLoanCards(
    uiState: HomeUiState,
    onAjukan: () -> Unit,
    onLihatStatus: () -> Unit,
    onBayarSekarang: () -> Unit,
    onLengkapiProfil: (ProfileRequirement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        LimitPlafondCard(
            tierPlafond = uiState.tierPlafond,
            plafondTotal = uiState.plafondTotal,
            sisaPlafond = uiState.sisaPlafond
        )
        if (uiState.missingRequirements.isNotEmpty()) {
            ProfileCompletenessCard(missing = uiState.missingRequirements, onItemClick = onLengkapiProfil)
        }
        uiState.activeLoan?.let { loan ->
            PengajuanAktifCard(loan = loan, onLihatStatus = onLihatStatus)
        }
        uiState.tagihanPreview?.let { tagihan ->
            TagihanBulanIniCard(tagihan = tagihan, onBayarSekarang = onBayarSekarang)
        }
        // Satu pengajuan aktif dulu sampai selesai direview - tombol Ajukan disembunyiin selama
        // masih ada yang di pipeline (sisa plafond tetap kelihatan di kartu atas).
        if (uiState.activeLoan == null) {
            GradientButton(text = "Ajukan Pinjaman", onClick = onAjukan, gradient = PengajuanButtonGradient)
        }
    }
}

// Angka yang "naik" dari 0 ke target waktu pertama muncul (atau waktu nilainya berubah).
@Composable
private fun animatedAmount(target: Double): Double {
    val animatable = remember { Animatable(0f) }
    LaunchedEffect(target) {
        animatable.animateTo(target.toFloat(), animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing))
    }
    return animatable.value.toDouble()
}

@Composable
private fun LimitPlafondCard(tierPlafond: String?, plafondTotal: Double?, sisaPlafond: Double?) {
    val style = tierStyleOf(tierPlafond)
    val total = (plafondTotal ?: 0.0).coerceAtLeast(0.0)
    val sisa = (sisaPlafond ?: total).coerceIn(0.0, total)
    val dipakai = (total - sisa).coerceAtLeast(0.0)
    val targetFraction = if (total > 0) (sisa / total).toFloat() else 0f
    val fraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "sisaPlafondFraction"
    )
    val sisaAnimated = animatedAmount(sisa)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(colors = style.gradient, start = Offset(0f, 0f), end = Offset(1000f, 1000f)))
            .border(1.dp, style.accent.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.koi_line),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.18f,
            modifier = Modifier.matchParentSize().offset(x = 60.dp, y = 10.dp)
        )
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Plafond tersedia",
                    color = Color.White.copy(alpha = 0.75f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = (tierPlafond ?: "Bronze").uppercase(),
                    color = style.accent,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.Black.copy(alpha = 0.25f))
                        .border(1.dp, style.accent.copy(alpha = 0.6f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = LoanCalculator.formatRupiah(sisaAnimated),
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.14f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(999.dp))
                        .background(style.accent)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = if (dipakai > 0) "${LoanCalculator.formatRupiah(dipakai)} sedang dipakai" else "Belum ada yang dipakai",
                    color = Color.White.copy(alpha = 0.6f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp
                )
                Text(
                    text = "dari ${plafondTotal?.let { LoanCalculator.formatRupiah(it) } ?: "-"}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun ProfileCompletenessCard(missing: List<ProfileRequirement>, onItemClick: (ProfileRequirement) -> Unit) {
    val total = ProfileRequirement.entries.size
    val done = total - missing.size
    val amber = Color(0xFFF2C25F)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(amber.copy(alpha = 0.08f))
            .border(1.dp, amber.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Lengkapi profilmu", color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "Wajib lengkap sebelum bisa mengajukan pinjaman",
                    color = Color.White.copy(alpha = 0.6f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp
                )
            }
            Text("$done/$total", color = amber, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(total) { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (i < done) amber else Color.White.copy(alpha = 0.15f))
                )
            }
        }
        ProfileRequirement.entries.forEach { req ->
            val isDone = req !in missing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = !isDone) { onItemClick(req) }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isDone) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isDone) BlobDark else Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = req.label,
                    color = if (isDone) Color.White.copy(alpha = 0.5f) else Color.White,
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                if (!isDone) {
                    Text("Isi", color = amber, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = amber, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun PengajuanAktifCard(loan: PengajuanMeResponse, onLihatStatus: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassFill)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pengajuan Aktif · ${LoanCalculator.formatPengajuanRef(loan.id)}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = LoanCalculator.formatRupiah(loan.nominalDisetujui ?: loan.nominalPengajuan),
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(BlobDark.copy(alpha = 0.18f))
                    .clickable(onClick = onLihatStatus)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text("Lihat Status", color = BlobDark, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        PengajuanStepper(status = loan.status)
    }
}

@Composable
private fun TagihanBulanIniCard(tagihan: TagihanPreview, onBayarSekarang: () -> Unit) {
    val days = tagihan.daysUntilDue
    // Makin dekat jatuh tempo, makin "panas" warnanya: > 7 hari hijau, 3-7 kuning, <= 2 merah.
    val urgencyColor = when {
        days == null -> Color.White.copy(alpha = 0.5f)
        days <= 2 -> Color(0xFFFF6B6B)
        days <= 7 -> Color(0xFFF2C25F)
        else -> BlobDark
    }
    val countdownText = when {
        days == null -> null
        days <= 0L -> "Jatuh tempo hari ini"
        days == 1L -> "Jatuh tempo besok"
        else -> "Jatuh tempo $days hari lagi"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassFill)
            .border(1.dp, if (days != null && days <= 7) urgencyColor.copy(alpha = 0.45f) else GlassBorder, RoundedCornerShape(20.dp))
            .padding(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Tagihan Bulan Ini", color = Color.White.copy(alpha = 0.6f), fontFamily = PlusJakartaSans, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = LoanCalculator.formatRupiah(tagihan.cicilanBulanan),
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            if (countdownText != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = countdownText + (tagihan.dueDateLabel?.let { " · $it" } ?: ""),
                    color = urgencyColor,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Brush.horizontalGradient(listOf(ButtonTurquoiseLight, ButtonTurquoiseDeep)))
                .clickable(onClick = onBayarSekarang)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text("Bayar Sekarang", color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

private fun greetingForNow(hour: Int = LocalTime.now().hour): String = when (hour) {
    in 4..10 -> "Selamat pagi,"
    in 11..14 -> "Selamat siang,"
    in 15..17 -> "Selamat sore,"
    else -> "Selamat malam,"
}

@Composable
private fun TopBar(
    onNavigateToLogin: () -> Unit,
    customerName: String? = null,
    unreadNotifCount: Int = 0,
    onBellClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.screenTitleInset().fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            if (customerName != null) {
                Text(
                    text = greetingForNow(),
                    color = Color.White.copy(alpha = 0.65f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp
                )
                Text(
                    text = "$customerName 👋",
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            } else {
                Text(
                    text = "Selamat datang di",
                    color = Color.White.copy(alpha = 0.65f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp
                )
                Text(
                    text = "Saku-ku",
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }
        }
        // Lonceng notifikasi - cuma buat user yang udah login (tamu gak punya notifikasi).
        if (customerName != null) {
            Box {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GlassFill)
                        .border(1.dp, GlassBorder, CircleShape)
                        .clickable(onClick = onBellClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.NotificationsNone,
                        contentDescription = "Notifikasi",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                if (unreadNotifCount > 0) {
                    Text(
                        text = if (unreadNotifCount > 9) "9+" else unreadNotifCount.toString(),
                        color = Color(0xFF04160F),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-2).dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(BlobDark)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
        }
//        Row(
//            modifier = Modifier
//                .clip(RoundedCornerShape(999.dp))
//                .background(GlassFill)
//                .border(1.dp, GlassBorder, RoundedCornerShape(999.dp))
//                .clickable(onClick = onNavigateToLogin)
//                .padding(horizontal = 16.dp, vertical = 10.dp)
//        ) {
//            Text(
//                text = "Masuk / Daftar",
//                color = Color.White,
//                fontFamily = PlusJakartaSans,
//                fontWeight = FontWeight.SemiBold,
//                fontSize = 12.5.sp
//            )
//        }
    }
}

private data class HeroSlide(@DrawableRes val imageRes: Int, val title: String, val subtitle: String, val showButton: Boolean)

private val heroSlides = listOf(
    HeroSlide(R.drawable.money, "Pinjaman tunai\ndana segar", "Langsung cair ke rekeningmu", showButton = true),
    HeroSlide(R.drawable.stack_cash, "Saat dompet kritis\nmelanda", "Saku-ku selalu ada", showButton = false),
    HeroSlide(R.drawable.announcement, "Kini Saku-ku\ntelah rilis", "Ayo dukung saku-ku", showButton = false),
    HeroSlide(R.drawable.shopping, "Buat pinjaman\nsekarang", "Untuk penuhi kebutuhanmu", showButton = false)
)

@Composable
private fun HeroBannerSection(onNavigateToLogin: () -> Unit, isLoggedIn: Boolean) {
    val pagerState = rememberPagerState(pageCount = { heroSlides.size })

    LaunchedEffect(Unit) {
        while (true) {
            delay(4.seconds)
            if (!pagerState.isScrollInProgress) {
                val nextPage = (pagerState.currentPage + 1) % heroSlides.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
            val slide = heroSlides[page]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF0B3B2E), Color(0xFF0F2B22))))
                    .padding(start = 20.dp, top = 20.dp, bottom = 20.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(
                        text = slide.title,
                        color = Color.White,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        lineHeight = 21.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = slide.subtitle,
                        color = Color.White.copy(alpha = 0.65f),
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.5.sp
                    )

                    if (slide.showButton && !isLoggedIn) {

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        ButtonTurquoiseLight,
                                        ButtonTurquoiseDeep
                                    )
                                )
                            )
                            .clickable(onClick = onNavigateToLogin)
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Gabung sekarang",
                            color = Color.White,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp),
//                        .size(64.dp)
//                        .clip(RoundedCornerShape(18.dp))
//                        .background(Color.White.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
//                    Icon(imageVector = slide.icon, contentDescription = null, tint = BlobLight, modifier = Modifier.size(30.dp))
                    Image(
                        painter = painterResource(id = slide.imageRes),
                        contentDescription = "Illustrasi Banner",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            heroSlides.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.currentPage) BlobDark else Color.White.copy(
                                alpha = 0.25f
                            )
                        )
                )
            }
        }
    }
}

private enum class PromoBannerType(val icon: ImageVector, val accent: Color) {
    ANNOUNCEMENT(Icons.Rounded.Campaign, BlobDark),
    MARKETING(Icons.AutoMirrored.Rounded.TrendingUp, ButtonTurquoiseLight),
    SHOPPING(Icons.Rounded.ShoppingBag, BlobMid)
}

private data class PromoBannerItem(@DrawableRes val imageRes: Int, val text: String, val type: PromoBannerType)

private val promoBanners = listOf(
    PromoBannerItem(R.drawable.announcement, "Dana Tunai Kini Cair Lebih Cepat ke Rekeningmu!", PromoBannerType.ANNOUNCEMENT),
    PromoBannerItem(
        R.drawable.marketing,"Bayar cicilan tepat waktu bulan ini dan nikmati kenaikan limit otomatis hingga Rp 20 Juta.",
        PromoBannerType.MARKETING
    ),
    PromoBannerItem(R.drawable.shopping,"Penuhi segala kebutuhan dengan tenang menggunakan Saku-ku", PromoBannerType.SHOPPING)
)

@Composable
private fun PromoBannerSection() {
    val pagerState = rememberPagerState(pageCount = { promoBanners.size })

    LaunchedEffect(Unit) {
       while (true){
           delay(4.seconds)
           if (!pagerState.isScrollInProgress && pagerState.pageCount > 0){
               val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
               pagerState.animateScrollToPage(nextPage)
           }
       }
    }

    Column {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
            val bannerSlide = promoBanners[page]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF0B3B2E), Color(0xFF0F2B22))))
                    .padding(start = 20.dp, top = 20.dp, bottom = 20.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(
                        text = bannerSlide.text,
                        color = Color.White,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        lineHeight = 21.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = bannerSlide.imageRes),
                        contentDescription = "Illustrasi Banner",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            promoBanners.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.currentPage) BlobDark else Color.White.copy(
                                alpha = 0.25f
                            )
                        )
                )
            }
        }
    }
}


@Composable
private fun PromoBannerCard(item: PromoBannerItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(item.type.accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.type.icon,
                contentDescription = null,
                tint = item.type.accent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = item.text,
            color = Color.White.copy(alpha = 0.85f),
            fontFamily = PlusJakartaSans,
            fontSize = 12.5.sp,
            lineHeight = 17.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BenefitsSection() {
    Column {
        SectionHeader(title = "Keuntungan Pakai Saku-ku")
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier
            .fillMaxWidth()
            .height(
                IntrinsicSize.Max
            )) {
            InfoCard("Proses Cepat", "Pencairan cepat", Icons.Rounded.Bolt, Modifier
                .weight(1f)
                .fillMaxSize())
            InfoCard("Bunga Rendah", "3% per tahun", Icons.Rounded.Percent, Modifier
                .weight(1f)
                .fillMaxSize())
            InfoCard("Transparan", "status pengajuan langsung", Icons.Rounded.Search, Modifier
                .weight(1f)
                .fillMaxSize())
        }
    }
}

@Composable
private fun ApplicationFlowSection(maxNominal: Double) {
    Column {
        SectionHeader(title = "Alur Pengajuan")
        Spacer(modifier = Modifier.height(14.dp))
//        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier
//            .fillMaxWidth()
//            .horizontalScroll(rememberScrollState())) {
//            val cardModifier = Modifier.width(140.dp)
//            InfoCard("Isi Data Diri", "Lengkapi detail data diri", Icons.Rounded.Edit, cardModifier, stepNumber = 1)
//            InfoCard("Dapatkan Limit", "Hingga ${LoanCalculator.formatRupiah(maxNominal)}", Icons.Rounded.Speed, cardModifier, stepNumber = 2)
//            InfoCard("Ajukan", "Ajukan pinjaman kamu", Icons.AutoMirrored.Rounded.Send, cardModifier, stepNumber = 3)
//            InfoCard("Dana Cair", "Dana akan segera cair", Icons.Rounded.AttachMoney, cardModifier, stepNumber = 4)
//        }
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val cardModifier = Modifier.weight(1f)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                InfoCard("Isi Data Diri", "Lengkapi detail data diri", Icons.Rounded.Edit, cardModifier, stepNumber = 1)
                InfoCard("Dapatkan Limit", "Hingga ${LoanCalculator.formatRupiah(maxNominal)}", Icons.Rounded.Speed, cardModifier, stepNumber = 2)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                InfoCard("Ajukan", "Ajukan pinjaman kamu", Icons.AutoMirrored.Rounded.Send, cardModifier, stepNumber = 3)
                InfoCard("Dana Cair", "Dana akan segera cair", Icons.Rounded.AttachMoney, cardModifier, stepNumber = 4)
            }

        }
    }
}

@Composable
private fun InfoCard(title: String, subtitle: String, icon: ImageVector, modifier: Modifier = Modifier, stepNumber: Int? = null) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GlassFill)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        )
        {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BlobDark,
                    modifier = Modifier.size(15.dp)
                )
            }

            if (stepNumber != null) {
                Text(
                    text = "#$stepNumber",
                    color = BlobDark,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                lineHeight = 14.sp
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.5f),
                fontFamily = PlusJakartaSans,
                fontSize = 9.5.sp,
                lineHeight = 12.sp
            )
        }

    }
}

@Composable
private fun TrustFooterSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "Mengacu pada regulasi perbankan & OJK",
            color = Color.White.copy(alpha = 0.35f),
            fontFamily = PlusJakartaSans,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.AccountBalance, contentDescription = "Mitra perbankan", tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
            Icon(Icons.Rounded.Shield, contentDescription = "Regulasi OJK", tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun PlafondPreviewSection(tiers: List<PlafondResponse>, isLoading: Boolean) {
    Column {
        SectionHeader(
            title = "Plafond yang bisa didapatkan",
            subtitle = "Dihitung otomatis dari profilmu saat daftar"
        )
        Spacer(modifier = Modifier.height(14.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = BlobDark, strokeWidth = 2.dp)
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                tiers.forEach { tier -> TierMiniCard(tier) }
            }
        }
    }
}

@Composable
private fun TierMiniCard(tier: PlafondResponse) {
    val accent = tierColor(tier.namaPlafond)
    Column(
        modifier = Modifier
            .widthIn(min = 168.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(GlassFill)
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = tier.namaPlafond.uppercase(),
            color = accent,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 10.5.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(accent.copy(alpha = 0.18f))
                .padding(horizontal = 9.dp, vertical = 3.dp)
        )
        Column {
            Text(
                text = "Hingga",
                color = Color.White.copy(alpha = 0.45f),
                fontFamily = PlusJakartaSans,
                fontSize = 9.5.sp
            )
            Text(
                text = LoanCalculator.formatRupiah(tier.limitMaksimal),
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        tier.deskripsi?.let {
            Text(
                text = it,
                color = Color.White.copy(alpha = 0.55f),
                fontFamily = PlusJakartaSans,
                fontSize = 10.sp,
                lineHeight = 13.sp
            )
        }
    }
}

private data class QuickAccessItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val isNew: Boolean = false
)

@Composable
private fun QuickAccessSection(
    isLoggedIn: Boolean,
    onDaftarMasuk: () -> Unit,
    onDaftar: () -> Unit = {},
    onSimulasi: () -> Unit,
    onCekPlafond: () -> Unit,
    onAjukan: () -> Unit,
    onRiwayat: () -> Unit = {},
    onRekening: () -> Unit = {},
    onCaraBayar: () -> Unit = {},
    onBantuan: () -> Unit = {}
) {
    //before after login
    val items = buildList {
        if (isLoggedIn) {
            add(QuickAccessItem("Riwayat", Icons.Rounded.History, onRiwayat))
            add(QuickAccessItem("Rekening", Icons.Rounded.AccountBalance, onRekening))
            add(QuickAccessItem("Cara Bayar", Icons.Rounded.Payments, onCaraBayar))
            add(QuickAccessItem("Cek Plafond", Icons.Rounded.PriceChange, onCekPlafond, isNew = false))
            add(QuickAccessItem("Pusat Bantuan", Icons.AutoMirrored.Rounded.HelpOutline, onBantuan))
        } else {
            add(QuickAccessItem("Daftar / Masuk", Icons.AutoMirrored.Rounded.ArrowForward, onDaftarMasuk))
            add(QuickAccessItem("Ajukan Pinjaman", Icons.AutoMirrored.Rounded.Send, onDaftar))
            add(QuickAccessItem("Simulasi Pinjaman", Icons.Rounded.Calculate, onSimulasi))
            add(QuickAccessItem("Cek Plafond", Icons.Rounded.PriceChange, onCekPlafond, isNew = true))
        }
    }

    Column {
        SectionHeader(title = "Akses cepat")
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(GlassFill)
                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
//                .horizontalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { item ->
                QuickAccessTile(item.label, item.icon, Modifier.width(72.dp), isNew = item.isNew, onClick = item.onClick)
            }
        }
    }
}

@Composable
private fun QuickAccessTile(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isNew: Boolean = false,
    onClick: () -> Unit
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(BlobDark.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = BlobDark, modifier = Modifier.size(20.dp))
            }
            Text(
                text = label,
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 13.sp
            )
        }
        if (isNew) {
            Text(
                text = "BARU",
                color = Color(0xFF04160F),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Black,
                fontSize = 8.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(6.dp))
                    .background(BlobDark)
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }
    }
}
@Composable
private fun SectionHeader(title: String, subtitle: String? = null) {
    Column {
        Text(
            text = title,
            color = Color.White,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )
        subtitle?.let {
            Text(
                text = it,
                color = Color.White.copy(alpha = 0.5f),
                fontFamily = PlusJakartaSans,
                fontSize = 11.sp
            )
        }
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true, heightDp = 1400)
@Composable
private fun HomeScreenPreview() {
    SakukuTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                isLoading = false,
                tiers = listOf(
                    PlafondResponse("1", "Bronze", "Tier minimum", "REGULER", 2_000_000.0, "ACTIVE"),
                    PlafondResponse("2", "Silver", "Tier menengah bawah", "REGULER", 7_500_000.0, "ACTIVE"),
                    PlafondResponse("3", "Gold", "Tier menengah atas", "REGULER", 15_000_000.0, "ACTIVE")
                ),
                tenors = listOf(
                    BungaTenorResponse("t1", 6, 3.0, "ACTIVE"),
                    BungaTenorResponse("t2", 12, 8.0, "ACTIVE"),
                    BungaTenorResponse("t3", 18, 10.0, "ACTIVE")
                ),
                selectedTenorId = "t1",
                nominal = 1_000_000.0
            ),
            onNominalChange = {},
            onTenorSelect = {},
            onRetry = {},
            onNavigateToLogin = {},
            onNavigateToPlafond = {},
            onNavigateToApply = {},
            onNavigateToRiwayat = {},
            onNavigateToRekening = {},
            onNavigateToBayar = {},
            onNavigateToBantuan = {},
            onNavigateToStatusDetail = {}
        )
    }
}
