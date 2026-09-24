package com.example.sakuku.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakuku.R
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.sakukuBlobBackground
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val image: Int,
    val title: String
)

private val onboardingPages = listOf(
    OnboardingPage(R.drawable.plane_flying, "Temukan dana daruratmu"),
    OnboardingPage(R.drawable.journey, "Dimanapun Kapanpun"),
    OnboardingPage(R.drawable.community, "Yuk, Jadi Bagian dari Saku-ku")
)

@Composable
fun OnboardingScreen(
    onSkip: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingPages.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .sakukuBlobBackground()
            // enableEdgeToEdge() aktif di MainActivity - tanpa ini, row Lewati/Register
            // ketiban 3-button system nav bar (ketemu 13 Sept, HP Android 14/API 34).
            .navigationBarsPadding()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val onboardingPage = onboardingPages[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = onboardingPage.image),
                    contentDescription = onboardingPage.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = onboardingPage.title,
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    onboardingPages.indices.forEach { index ->
                        val isActive = index == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .size(if (isActive) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (isActive) BlobDark else Color.White.copy(alpha = 0.3f))
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lewati",
                color = Color.White.copy(alpha = 0.7f),
                fontFamily = PlusJakartaSans,
                fontSize = 14.sp,
                modifier = Modifier.clickable(onClick = onSkip)
            )

            if (isLastPage) {
                // Dulu langsung "Register" - sekarang CTA utama "Masuk" (user yang udah punya
                // akun didahuluin), Daftar jadi link sekunder di bawah (pola sama kayak link
                // "Belum punya akun? Ayo Daftar" di LoginScreen).
                Text(
                    text = "Masuk",
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable(onClick = onNavigateToLogin)
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "Selanjutnya",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                )
            }
        }

        if (isLastPage) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Belum punya akun? ",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    fontFamily = PlusJakartaSans
                )
                Text(
                    text = "Daftar",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = PlusJakartaSans,
                    modifier = Modifier.clickable(onClick = onNavigateToRegister)
                )
            }
        }
    }
}
