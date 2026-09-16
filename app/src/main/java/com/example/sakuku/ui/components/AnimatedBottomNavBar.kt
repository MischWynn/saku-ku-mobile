package com.example.sakuku.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakuku.ui.theme.PlusJakartaSans // Ganti dengan path Type.kt milikmu[cite: 2]
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.HazeBlurStyle

// GlassBg diturunin dari 0.75 (nyaris solid, blob di belakangnya nyaris gak nembus) ke 0.42 -
// beneran blur backdrop (yang niru CSS backdrop-filter) gak ada built-in di Compose polos tanpa
// nambah library (mis. Haze), jadi opacity yang lebih rendah + sheen gradient di bawah ini
// pendekatan yang paling murah/aman buat kesan glass tanpa dependency baru.

//val GlassBg = Color(0xFF151716).copy(alpha = 0.60f)

val GlassBorder = Color.White.copy(alpha = 0.16f)

// Highlight diagonal tipis (kiri-atas ke kanan-bawah) - trik umum bikin permukaan flat kerasa
// kayak kaca, tanpa perlu blur beneran.

//val GlassSheen = Brush.linearGradient(
//    colors = listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0f)),
//    start = Offset(0f, 0f),
//    end = Offset(600f, 300f)
//)
val ItemUnselectedBg = Color.White.copy(alpha = 0.1f)
val ItemSelectedBg = Color(0xFF20D09B).copy(alpha = 0.9f)

data class NavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

val navItems = listOf(
    NavItem("Beranda", Icons.Rounded.Cottage, "home"),
    NavItem("Riwayat", Icons.Rounded.ManageSearch, "history"),
    NavItem("Ajukan", Icons.Rounded.Payments, "apply"),
    NavItem("Notif", Icons.Rounded.FactCheck, "notification"),
    NavItem("Profil", Icons.Rounded.Person, "profile")
)

@Composable
fun AnimatedBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    hazeState: HazeState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 16.dp)
            .clip(CircleShape)
            // Efek Glassmorphism Induk - background transparan dulu, baru sheen di atasnya,
            // baru border (urutan modifier nentuin layering visual).
//            .background(GlassBg)
//            .background(GlassSheen)
            .hazeBlur(
                input = HazeInput.Sources(hazeState),
                style = HazeBlurStyle {
                    blurRadius(16.dp)
                    backgroundColor(Color.White.copy(alpha = 0.05f))
                }

//                     tint = HazeTint(Color.White.copy(alpha = 0.05f))

            )
            .border(width = 1.dp, color = GlassBorder, shape = CircleShape)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        navItems.forEach { item ->
            val isSelected = currentRoute == item.route
            NavBarItem(
                item = item,
                isSelected = isSelected,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}

@Composable
fun NavBarItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) ItemSelectedBg else ItemUnselectedBg,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 350)
    )

    Row(
        modifier = Modifier
            .clip(CircleShape)
            // Efek border & fill untuk anak item
            .background(bgColor)
            .border(
                width = 1.dp,
                color = if (isSelected) Color.Transparent else GlassBorder,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = if (isSelected) 16.dp else 12.dp, vertical = 12.dp)
            .animateContentSize(
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )

        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(animationSpec = tween(350))
        ) {
            Row {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.title,
                    color = contentColor,
                    fontSize = 13.sp,
                    fontFamily = PlusJakartaSans, // Merujuk pada file font yang sudah dimasukkan[cite: 2]
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}