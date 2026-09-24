package com.example.sakuku.ui.components

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.sakuku.ui.theme.PlusJakartaSans

// Selain lewat slider, jumlah pinjaman sekarang bisa langsung diketik pakai numpad HP - dipakai
// bareng di HomeScreen (simulasi tamu) dan PengajuanScreen (step 1) biar konsisten, keduanya
// baca/nulis ke state Double yang sama dengan slider di sebelahnya. Angka mentah (tanpa "Rp"/titik
// ribuan) yang disimpan sebagai state; tampilan "Rp1.000.000"-nya cuma efek VisualTransformation,
// jadi cursor gak berantakan tiap kali posisi separator titik berubah pas ngetik.
@Composable
fun EditableNominalField(
    nominal: Double,
    onNominalChange: (Double) -> Unit,
    minNominal: Double,
    maxNominal: Double,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 26.sp,
    enabled: Boolean = true
) {
    var fieldValue by remember {
        mutableStateOf(TextFieldValue(nominal.toLong().toString()))
    }

    // Sinkron balik kalau nominal berubah dari luar (slider digeser) - tapi cuma kalau angkanya
    // beneran beda dari yang lagi diketik, biar teks yang lagi diketik user gak keputus/ke-reset.
    LaunchedEffect(nominal) {
        if (fieldValue.text.toDoubleOrNull() != nominal) {
            val text = nominal.toLong().toString()
            fieldValue = TextFieldValue(text = text, selection = TextRange(text.length))
        }
    }

    BasicTextField(
        value = fieldValue,
        onValueChange = { new ->
            val digits = new.text.filter { it.isDigit() }.take(12)
            fieldValue = new.copy(text = digits)
            onNominalChange(digits.toDoubleOrNull() ?: 0.0)
        },
        enabled = enabled,
        singleLine = true,
        textStyle = TextStyle(
            color = Color.White,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize
        ),
        cursorBrush = SolidColor(Color.White),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = RupiahVisualTransformation(),
        modifier = modifier.onFocusChanged { focusState ->
            // Blur = selesai ngetik: clamp ke range yang sama kayak slider, biar gak bisa nyangkut
            // di luar batas plafond/min cuma karena user ngetik angka aneh terus ditinggal pergi.
            if (!focusState.isFocused) {
                onNominalChange(nominal.coerceIn(minNominal, maxNominal))
            }
        }
    )
}

// Bukan private lagi - dipakai juga sebagai `visualTransformation` param di SakukuOutlinedField
// buat field pendapatan bulanan (Register/DataPekerjaan/EditDataDiri), biar angka yang diketik
// nampil "Rp1.000.000" bukan "1000000" mentah, konsisten sama slider nominal pinjaman di Home.
class RupiahVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        val sb = StringBuilder("Rp")
        val original2Transformed = IntArray(original.length + 1)
        for (i in original.indices) {
            original2Transformed[i] = sb.length
            val fromEnd = original.length - i
            if (fromEnd % 3 == 0 && i != 0) sb.append('.')
            sb.append(original[i])
        }
        original2Transformed[original.length] = sb.length

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                original2Transformed.getOrElse(offset.coerceIn(0, original.length)) { sb.length }

            override fun transformedToOriginal(offset: Int): Int {
                var result = 0
                for (i in original2Transformed.indices) {
                    if (original2Transformed[i] <= offset) result = i else break
                }
                return result
            }
        }
        return TransformedText(AnnotatedString(sb.toString()), offsetMapping)
    }
}
