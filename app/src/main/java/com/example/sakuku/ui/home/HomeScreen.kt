@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.sakuku.ui.home

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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material.icons.rounded.PriceChange
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.sakuku.data.remote.dto.PlafondResponse
import com.example.sakuku.ui.components.AuthButtonGradient
import com.example.sakuku.ui.components.EditableNominalField
import com.example.sakuku.ui.components.GradientButton
import com.example.sakuku.ui.components.RoundSliderThumb
import com.example.sakuku.ui.components.SelectableChip
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.BlobLight
import com.example.sakuku.ui.theme.ButtonTurquoiseDeep
import com.example.sakuku.ui.theme.ButtonTurquoiseLight
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.sakukuBlobBackground
import com.example.sakuku.ui.theme.SakukuTheme
import com.example.sakuku.util.LoanCalculator
import kotlinx.coroutines.launch
import androidx.annotation.DrawableRes
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.IntrinsicSize

private val GlassFill = Color.White.copy(alpha = 0.05f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)

// Warna badge per tier - dicocokin ke nama asli dari tbl_plafond (Bronze/Silver/Gold/Platinum).
// Tier di luar 4 nama ini (kalau nanti ditambah) jatuh ke abu-abu netral, bukan crash.
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
    onNavigateToPlafond: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreenContent(
        uiState = uiState,
        onNominalChange = viewModel::onNominalChange,
        onTenorSelect = viewModel::onTenorSelect,
        onRetry = viewModel::retry,
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToPlafond = onNavigateToPlafond
    )
}

@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onNominalChange: (Double) -> Unit,
    onTenorSelect: (String) -> Unit,
    onRetry: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToPlafond: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val simulasiAnchor = remember { BringIntoViewRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .sakukuBlobBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                // bottom digedein (bukan 32.dp biasa) - navbar sekarang floating beneran di atas
                // konten (lihat MainScreen), jadi butuh jarak aman biar kartu Estimasi/tombol
                // terakhir gak ketutup pill navbar.
                .padding(top = 24.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            TopBar(onNavigateToLogin = onNavigateToLogin)

            HeroBannerSection(onNavigateToLogin = onNavigateToLogin)

            QuickAccessSection(
                onDaftarMasuk = onNavigateToLogin,
                onSimulasi = { coroutineScope.launch { simulasiAnchor.bringIntoView() } },
                onCekPlafond = onNavigateToPlafond
            )

            BenefitsSection()

            ApplicationFlowSection(maxNominal = uiState.maxNominal)

            PlafondPreviewSection(tiers = uiState.tiers, isLoading = uiState.isLoading)

            SimulasiSection(
                modifier = Modifier.bringIntoViewRequester(simulasiAnchor),
                uiState = uiState,
                onNominalChange = onNominalChange,
                onTenorSelect = onTenorSelect,
                onRetry = onRetry,
                onNavigateToLogin = onNavigateToLogin
            )

            TrustFooterSection()
        }
    }
}

@Composable
private fun TopBar(onNavigateToLogin: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
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
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
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

// Konten teks/icon 2 slide promo - TIDAK pakai ilustrasi karakter asli dari Figma (belum ada
// aset PNG-nya di res/drawable/, cuma placeholder icon Material dulu). Begitu asetnya
// diekspor dari Figma, tinggal ganti Icon(...) di HeroBannerSection jadi Image(painterResource)
// kayak pola di WelcomeScreen/OnboardingScreen, teks/layout gak perlu berubah.
private data class HeroSlide(@DrawableRes val imageRes: Int, val title: String, val subtitle: String, val showButton: Boolean)

private val heroSlides = listOf(
    HeroSlide(R.drawable.money, "Pinjaman tunai\ndana segar", "Langsung cair ke rekeningmu", showButton = true),
    HeroSlide(R.drawable.stack_cash, "Saat dompet kritis\nmelanda", "Saku-ku selalu ada", showButton = false)
)

@Composable
private fun HeroBannerSection(onNavigateToLogin: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { heroSlides.size })

    LaunchedEffect(key1 = pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress){
            while (true) {
                delay(4000)
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
                    if(slide.showButton) {

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

@Composable
private fun BenefitsSection() {
    Column {
        SectionHeader(title = "Keuntungan Pakai Saku-ku")
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().height(
            IntrinsicSize.Max)) {
            InfoCard("Proses Cepat", "Pencairan cepat", Icons.Rounded.Bolt, Modifier.weight(1f).fillMaxSize())
            InfoCard("Bunga Rendah", "3% per tahun", Icons.Rounded.Percent, Modifier.weight(1f).fillMaxSize())
            InfoCard("Transparan", "status pengajuan langsung", Icons.Rounded.Search, Modifier.weight(1f).fillMaxSize())
        }
    }
}

@Composable
private fun ApplicationFlowSection(maxNominal: Double) {
    Column {
        SectionHeader(title = "Alur Pengajuan")
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())) {
            val cardModifier = Modifier.width(140.dp)
            InfoCard("Isi Data Diri", "Lengkapi detail data diri", Icons.Rounded.Edit, cardModifier, stepNumber = 1)
            InfoCard("Dapatkan Limit", "Hingga ${LoanCalculator.formatRupiah(maxNominal)}", Icons.Rounded.Speed, cardModifier, stepNumber = 2)
            InfoCard("Ajukan", "Ajukan pinjaman kamu", Icons.AutoMirrored.Rounded.Send, cardModifier, stepNumber = 3)
            InfoCard("Dana Cair", "Dana akan segera cair", Icons.Rounded.AttachMoney, cardModifier, stepNumber = 4)
        }
    }
}

// Dipakai bareng buat kartu "Keuntungan" & "Alur Pengajuan" - visualnya identik (icon+judul+
// subjudul), cuma beda konten, gak perlu 2 composable kartu terpisah.
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

// Sengaja BUKAN logo BCA/OJK asli dari mockup - project ini simulasi bootcamp, bukan produk
// finansial berlisensi beneran (lihat CLAUDE.md), jadi make logo bank/regulator asli buat
// klaim afiliasi/pengawasan berisiko menyesatkan (juga soal trademark). Placeholder icon
// generik dulu - kalau nanti beneran ada kerja sama/butuh tampil "meyakinkan" buat demo,
// diskusiin dulu sebelum swap ke logo asli.
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
            // widthIn (bukan width tetap) - kartu punya lebar dasar lebih lega, tapi tetap bisa
            // "elastis" ngikutin kalau teks di dalamnya butuh ruang lebih (mis. font sistem HP
            // di-set besar via Setelan Aksesibilitas) - semua Text di sini udah pakai .sp jadi
            // otomatis ke-scale, kartu ini yang perlu ikut nyesuaiin.
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

@Composable
private fun QuickAccessSection(
    onDaftarMasuk: () -> Unit,
    onSimulasi: () -> Unit,
    onCekPlafond: () -> Unit
) {
    Column {
        SectionHeader(title = "Akses cepat")
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            QuickAccessTile("Daftar / Masuk", Icons.AutoMirrored.Rounded.ArrowForward, Modifier.weight(1f), onClick = onDaftarMasuk)
            QuickAccessTile("Simulasi Pinjaman", Icons.Rounded.Calculate, Modifier.weight(1f), onClick = onSimulasi)
            QuickAccessTile("Cek Plafond", Icons.Rounded.PriceChange, Modifier.weight(1f), onClick = onCekPlafond, isNew = true)
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
                .clip(RoundedCornerShape(18.dp))
                .background(GlassFill)
                .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = BlobDark, modifier = Modifier.size(17.dp))
            }
            Text(
                text = label,
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.5.sp,
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
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(BlobDark)
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun SimulasiSection(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onNominalChange: (Double) -> Unit,
    onTenorSelect: (String) -> Unit,
    onRetry: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Column(modifier = modifier) {
        SectionHeader(title = "Simulasi pinjaman")
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.errorMessage != null) {
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
                    text = "Coba lagi",
                    color = BlobDark,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    modifier = Modifier.clickable(onClick = onRetry)
                )
            }
            return
        }

        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Jumlah pinjaman", color = Color.White.copy(alpha = 0.75f), fontFamily = PlusJakartaSans, fontSize = 12.sp)
                    Text(
                        "maks. ${LoanCalculator.formatRupiah(uiState.maxNominal)}",
                        color = Color.White.copy(alpha = 0.4f),
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Customer login yang sisaPlafond-nya di bawah Rp500rb (batas minimum pinjaman)
                // bikin range 500rb..maxNominal jadi "kosong" (max < min) - Slider Compose
                // nge-throw IllegalArgumentException kalau itu kejadian. Turunin batas bawah ke
                // 0 buat kasus itu, sama pola kayak HomeViewModel.onNominalChange().
                val effectiveMinNominal = if (uiState.maxNominal < 500_000.0) 0.0 else 500_000.0
                EditableNominalField(
                    nominal = uiState.nominal,
                    onNominalChange = onNominalChange,
                    minNominal = effectiveMinNominal,
                    maxNominal = uiState.maxNominal
                )
                Slider(
                    value = uiState.nominal.toFloat(),
                    onValueChange = { onNominalChange(it.toDouble()) },
                    valueRange = effectiveMinNominal.toFloat()..uiState.maxNominal.toFloat(),
                    steps = 0,
                    thumb = { RoundSliderThumb() },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = BlobDark,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    )
                )
                Text(
                    text = "Geser sesuai kebutuhanmu",
                    color = Color.White.copy(alpha = 0.4f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 10.5.sp
                )
            }

            if (uiState.tenors.isNotEmpty()) {
                Column {
                    Text("Pilih tenor", color = Color.White.copy(alpha = 0.75f), fontFamily = PlusJakartaSans, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.tenors.forEach { tenor ->
                            SelectableChip(
                                modifier = Modifier.weight(1f),
                                label = "${tenor.tenor} Bulan",
                                subtitle = "${formatPercent(tenor.interestRate)}%",
                                isSelected = tenor.id == uiState.selectedTenorId,
                                onClick = { onTenorSelect(tenor.id) }
                            )
                        }
                    }
                }

                val tenor = uiState.selectedTenor
                if (tenor != null) {
                    val estimate = LoanCalculator.estimate(uiState.nominal, tenor.tenor, tenor.interestRate)
                    EstimateCard(estimate = estimate, nominal = uiState.nominal, tenor = tenor, onNavigateToLogin = onNavigateToLogin)
                }
            } else if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = BlobDark, strokeWidth = 2.dp)
            }
        }
    }
}

@Composable
private fun EstimateCard(
    estimate: com.example.sakuku.util.LoanEstimate,
    nominal: Double,
    tenor: BungaTenorResponse,
    onNavigateToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.28f))
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "ESTIMASI CICILAN BULANAN",
            color = Color.White.copy(alpha = 0.45f),
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
        Text(
            text = LoanCalculator.formatRupiah(estimate.cicilanBulanan),
            color = BlobLight,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            EstimateRow("Suku bunga (flat)", "${formatPercent(tenor.interestRate)}% / tenor")
            EstimateRow("Total pinjaman pokok", LoanCalculator.formatRupiah(nominal))
            EstimateRow("Total pembayaran", LoanCalculator.formatRupiah(estimate.totalPembayaran))
        }

        Spacer(modifier = Modifier.height(2.dp))
        GradientButton(text = "Masuk untuk ajukan", onClick = onNavigateToLogin)
        Text(
            text = "*Nominal dapat berubah sesuai kebijakan yang berlaku. Suku bunga flat ",
            color = Color.White.copy(alpha = 0.35f),
            fontFamily = PlusJakartaSans,
            fontSize = 9.sp,
            lineHeight = 12.sp
        )
    }
}

@Composable
private fun EstimateRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White.copy(alpha = 0.55f), fontFamily = PlusJakartaSans, fontSize = 11.5.sp)
        Text(value, color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String? = null) {
    Column {
        Text(
            text = title,
            color = Color.White,
            fontFamily = PlusJakartaSans,
            fontStyle = FontStyle.Italic,
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

// interestRate dari API itu angka persen mentah (3.0 = "3%"), bukan pecahan 0-1.
private fun formatPercent(rate: Double): String =
    if (rate == rate.toInt().toDouble()) rate.toInt().toString() else "%.1f".format(rate)

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
            onNavigateToPlafond = {}
        )
    }
}
