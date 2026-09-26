package com.example.sakuku.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.PlusJakartaSans
import java.io.File

// Capture foto KTP via kamera (ActivityResultContracts.TakePicture() + FileProvider) - dipakai
// bareng Register (langkah Identitas) dan Profil -> KTP & Data Diri. Gak pakai OCR, NIK tetap
// diisi manual - foto ini murni dokumen pendukung buat verifikasi staff.
//
// Foto yang udah kesimpen di server SENGAJA gak pernah ditampilin balik (keputusan produk, backend
// cuma balikin hasFotoKtp) - jadi `alreadyUploaded` cuma ngganti placeholder jadi tanda "sudah
// diunggah", bukan nampilin fotonya.
@Composable
fun KtpPhotoCapture(
    previewUri: Uri?,
    onCaptured: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    alreadyUploaded: Boolean = false,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    var captureUri by remember { mutableStateOf<Uri?>(null) }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) captureUri?.let(onCaptured)
    }
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createKtpCaptureUri(context)
            captureUri = uri
            takePicture.launch(uri)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.fillMaxWidth()) {
        val bitmap = remember(previewUri) {
            previewUri?.let { uri -> context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } }
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Foto KTP",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.6f)
                    .clip(RoundedCornerShape(16.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.6f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (alreadyUploaded) BlobDark.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                if (alreadyUploaded) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = BlobDark, modifier = Modifier.size(40.dp))
                        Text(
                            text = "Foto KTP sudah diunggah",
                            color = Color.White,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Icon(Icons.Rounded.CreditCard, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.height(48.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (enabled) {
            Text(
                text = "Pastikan seluruh bagian KTP kelihatan jelas dan gak buram.",
                color = Color.White.copy(alpha = 0.6f),
                fontFamily = PlusJakartaSans,
                fontSize = 12.5.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val permissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                        val uri = createKtpCaptureUri(context)
                        captureUri = uri
                        takePicture.launch(uri)
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (previewUri != null || alreadyUploaded) "Foto Ulang" else "Ambil Foto KTP",
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun createKtpCaptureUri(context: Context): Uri {
    val directory = File(context.cacheDir, "camera").apply { mkdirs() }
    val file = File.createTempFile("ktp_", ".jpg", directory)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

// Base64 buat field fotoKtp di PATCH customer/me. null kalau file-nya gagal dibaca.
fun encodeKtpPhoto(context: Context, uri: Uri): String? = try {
    context.contentResolver.openInputStream(uri)?.use { input ->
        Base64.encodeToString(input.readBytes(), Base64.NO_WRAP)
    }
} catch (e: Exception) {
    null
}
