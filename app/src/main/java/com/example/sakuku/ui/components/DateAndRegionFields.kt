@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.sakuku.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sakuku.data.remote.dto.WilayahItem
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.BlobMid
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.util.Validators
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ID_LOCALE = Locale.Builder().setLanguage("id").setRegion("ID").build()
private val DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", ID_LOCALE)

// Extracted 17 Sept dari KtpDataDiriScreen.kt (Profil) biar Register bisa reuse - value
// disimpen/dikirim sebagai ISO "yyyy-MM-dd" (format yang diterima CustomerUpdateRequest
// backend), ditampilin dalam format lokal ("14 Mei 1998").
@Composable
fun TanggalLahirField(value: String, onValueChange: (String) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val parsedDate = remember(value) { value.toLocalDateOrNull() }
    val displayText = parsedDate?.format(DISPLAY_DATE_FORMATTER) ?: ""

    // OutlinedTextField (bahkan readOnly=true) nyerep touch event-nya sendiri buat gesture
    // cursor/text-selection, jadi Modifier.clickable yang ditumpuk di atasnya sering gak
    // kepanggil. Fix: field-nya di-disable total (gak pernah dapet fokus/gesture internal) +
    // Box transparan di atasnya yang beneran nangkep klik.
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            enabled = false,
            placeholder = { Text("Pilih tanggal lahir", color = Color.White.copy(alpha = 0.35f), fontFamily = PlusJakartaSans) },
            trailingIcon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = Color.White.copy(alpha = 0.6f)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = BlobMid.copy(alpha = 0.5f),
                disabledTextColor = Color.White,
                disabledContainerColor = Color.White.copy(alpha = 0.03f),
                disabledPlaceholderColor = Color.White.copy(alpha = 0.35f),
                disabledTrailingIconColor = Color.White.copy(alpha = 0.6f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(14.dp))
                .clickable { showPicker = true }
        )
    }

    if (showPicker) {
        // Minimal umur 17 tahun (syarat KTP): tanggal setelah batas itu gak bisa dipilih, dan
        // kalau belum ada tanggal kepilih, kalender langsung kebuka di bulan batasnya (bukan
        // bulan sekarang yang semuanya ke-disable).
        val latestAllowed = remember { Validators.latestAllowedBirthDate() }
        val latestAllowedMillis = latestAllowed.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = parsedDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
            initialDisplayedMonthMillis = parsedDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
                ?: latestAllowedMillis,
            yearRange = 1900..latestAllowed.year,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= latestAllowedMillis
                override fun isSelectableYear(year: Int): Boolean = year <= latestAllowed.year
            }
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onValueChange(picked.toString())
                    }
                    showPicker = false
                }) {
                    Text("Pilih", color = BlobDark, fontFamily = PlusJakartaSans, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Batal", color = Color.White.copy(alpha = 0.6f), fontFamily = PlusJakartaSans)
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private fun String.toLocalDateOrNull(): LocalDate? = try {
    if (isBlank()) null else LocalDate.parse(this)
} catch (e: Exception) {
    null
}

// Satu dropdown buat 1 level wilayah (Provinsi/Kota/Kecamatan) - dipakai 3x berturut-turut buat
// cascading picker (pilih Provinsi -> Kota ke-enable & fetch -> Kecamatan ke-enable & fetch).
// Disabled selama parent level-nya belum dipilih (enabled=false), spinner kecil di trailing
// icon selama isLoading (lagi fetch dari API wilayah).
@Composable
fun RegionDropdownField(
    label: String,
    selectedName: String?,
    options: List<WilayahItem>,
    enabled: Boolean,
    isLoading: Boolean,
    onSelect: (WilayahItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = selectedName ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            placeholder = {
                Text(
                    if (!enabled) "Pilih dulu di atas" else "Pilih $label",
                    color = Color.White.copy(alpha = 0.35f),
                    fontFamily = PlusJakartaSans
                )
            },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = BlobDark, strokeWidth = 2.dp)
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled)
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BlobDark,
                unfocusedBorderColor = BlobMid.copy(alpha = 0.5f),
                disabledBorderColor = BlobMid.copy(alpha = 0.2f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = Color.White.copy(alpha = 0.4f),
                focusedContainerColor = Color.White.copy(alpha = 0.03f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                disabledContainerColor = Color.White.copy(alpha = 0.02f),
                disabledPlaceholderColor = Color.White.copy(alpha = 0.25f)
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
