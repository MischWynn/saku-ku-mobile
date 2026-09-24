package com.example.sakuku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.PlusJakartaSans

// Diekstrak dari OtpScreen.kt (17 Sept) supaya bisa dipakai bareng - Register sekarang punya
// step OTP-nya sendiri (bukan navigate ke layar terpisah lagi), pola kotak digit-nya sama persis.
@Composable
fun OtpCodeInput(code: String, onCodeChange: (String) -> Unit, length: Int = 6) {
    val focusRequester = remember { FocusRequester() }

    Box(modifier = Modifier.fillMaxWidth()) {
        BasicTextField(
            value = code,
            onValueChange = onCodeChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .focusRequester(focusRequester)
                .alpha(0f)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { focusRequester.requestFocus() }
        ) {
            repeat(length) { index ->
                val char = code.getOrNull(index)?.toString() ?: ""
                val isCurrent = index == code.length
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(0.85f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(
                            width = 1.dp,
                            color = if (isCurrent) BlobDark else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char,
                        color = Color.White,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}
