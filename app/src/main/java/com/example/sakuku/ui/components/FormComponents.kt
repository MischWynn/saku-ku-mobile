package com.example.sakuku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.BlobMid
import com.example.sakuku.ui.theme.ButtonTurquoiseDeep
import com.example.sakuku.ui.theme.ButtonTurquoiseLight
import com.example.sakuku.ui.theme.PlusJakartaSans

// Dipakai bersama di seluruh layar Auth (Login, Register, dst) biar konsisten "skema"-nya.
// Cyan-turquoise, samain kayak preview "Beranda & Cek Plafond" (13 Sept) - sengaja beda
// warna dari BlobMid/BlobDark yang tetep dipakai buat checkbox/border field lainnya.
val AuthButtonGradient = Brush.horizontalGradient(listOf(ButtonTurquoiseLight, ButtonTurquoiseDeep))

@Composable
fun FieldLabel(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.85f),
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
fun SakukuOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        readOnly = readOnly,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BlobDark,
            unfocusedBorderColor = BlobMid.copy(alpha = 0.5f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = BlobDark,
            focusedContainerColor = Color.White.copy(alpha = 0.03f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
        ),
        modifier = modifier.fillMaxWidth()
    )
}

// Thumb bulat polos buat Slider Material3 - default bawaan M3 itu pill kecil vertikal ("|"),
// dipakai bareng di Home (Simulasi) & Ajukan Pinjaman biar 2 slider itu konsisten bentuknya.
@Composable
fun RoundSliderThumb(size: Dp = 20.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(2.dp, CircleShape)
            .background(Color.White, CircleShape)
            .border(1.dp, BlobDark.copy(alpha = 0.3f), CircleShape)
    )
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(brush = AuthButtonGradient, shape = RoundedCornerShape(16.dp))
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = PlusJakartaSans
            )
        }
    }
}
