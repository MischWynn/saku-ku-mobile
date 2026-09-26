package com.example.sakuku.ui.screens.profil

import com.example.sakuku.util.Validators
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.local.TokenDataStore
import com.example.sakuku.data.remote.dto.CustomerUpdateRequest
import com.example.sakuku.data.remote.dto.WilayahItem
import com.example.sakuku.data.repository.CustomerRepository
import com.example.sakuku.data.repository.WilayahRepository
import com.example.sakuku.ui.components.encodeKtpPhoto
import com.example.sakuku.ui.screens.register.TipePekerjaan
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfilUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val namaLengkap: String = "",
    val nik: String = "",
    // NIK cuma bisa diisi SEKALI (backend nolak kalau udah ada) - nikInput dipakai selama nik
    // dari server masih kosong, setelah itu jadi strip read-only.
    val nikInput: String = "",
    // Foto KTP: hasFotoKtp dari server (fotonya sendiri gak pernah dikirim balik), preview = hasil
    // jepretan baru yang belum disimpan. fotoKtpLockReason != null -> gak boleh foto ulang.
    val hasFotoKtp: Boolean = false,
    val fotoKtpLockReason: String? = null,
    val fotoKtpPreviewUri: Uri? = null,
    val tanggalLahir: String = "",
    val email: String = "",
    val noHp: String = "",
    val alamat: String = "",
    val tipePekerjaan: TipePekerjaan? = null,
    val pekerjaan: String = "",
    val pendapatanBulanan: String = "",
    val plafond: Double? = null,
    val sisaPlafond: Double? = null,
    val tierPlafond: String? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val loggedOut: Boolean = false,
    // Domisili cascading dropdown (Provinsi/Kota/Kecamatan) - `alamat` di atas tetap dipakai
    // buat detail jalan/RT-RW, bukan diganti. selected* nyimpen WilayahItem (id+name) biar bisa
    // fetch child level-nya, backend sendiri cuma nyimpen nama (lihat CustomerEntity.provinsi).
    val provinsiList: List<WilayahItem> = emptyList(),
    val kotaList: List<WilayahItem> = emptyList(),
    val kecamatanList: List<WilayahItem> = emptyList(),
    val selectedProvinsi: WilayahItem? = null,
    val selectedKota: WilayahItem? = null,
    val selectedKecamatan: WilayahItem? = null,
    val isLoadingProvinsi: Boolean = false,
    val isLoadingKota: Boolean = false,
    val isLoadingKecamatan: Boolean = false,
    // Rekening Bank - tujuan pencairan dana
    val namaBank: String = "",
    val nomorRekening: String = "",
    val namaPemilikRekening: String = ""
)

@HiltViewModel
class ProfilViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val wilayahRepository: WilayahRepository,
    private val tokenDataStore: TokenDataStore,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfilUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadProvinsi()
            loadProfile()
        }
    }

    private suspend fun loadProvinsi() {
        _uiState.update { it.copy(isLoadingProvinsi = true, errorMessage = null) }
        wilayahRepository.getProvinces()
            .onSuccess { list -> _uiState.update { it.copy(provinsiList = list, isLoadingProvinsi = false) } }
            .onFailure {
                _uiState.update {
                    it.copy(
                        isLoadingProvinsi = false,
                        errorMessage = "Gagal memuat daftar provinsi. Cek koneksi internetmu, lalu coba lagi."
                    )
                }
            }
    }

    fun retryFetchProvinsi() {
        viewModelScope.launch { loadProvinsi() }
    }

    fun load() {
        viewModelScope.launch { loadProfile() }
    }

    private var hasLoadedOnce = false

    fun refreshIfLoaded() {
        if (hasLoadedOnce) load()
    }

    private suspend fun loadProfile() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        hasLoadedOnce = true
        customerRepository.getMe()
            .onSuccess { profile ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        namaLengkap = profile.namaLengkap,
                        nik = profile.nik ?: "",
                        hasFotoKtp = profile.hasFotoKtp,
                        fotoKtpLockReason = profile.fotoKtpLockReason,
                        fotoKtpPreviewUri = null,
                        tanggalLahir = profile.tanggalLahir ?: "",
                        email = profile.email ?: "",
                        noHp = profile.noHp ?: "",
                        alamat = profile.alamat ?: "",
                        tipePekerjaan = TipePekerjaan.entries.find { t -> t.apiValue == profile.tipePekerjaan },
                        pekerjaan = profile.pekerjaan ?: "",
                        pendapatanBulanan = profile.pendapatanBulanan?.let { p -> p.toLong().toString() } ?: "",
                        plafond = profile.plafond,
                        sisaPlafond = profile.sisaPlafond,
                        tierPlafond = profile.tierPlafond,
                        namaBank = profile.namaBank ?: "",
                        nomorRekening = profile.nomorRekening ?: "",
                        namaPemilikRekening = profile.namaPemilikRekening ?: ""
                    )
                }
                syncSelectedWilayah(profile.provinsi, profile.kota, profile.kecamatan)
            }
            .onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Gagal memuat profil") }
            }
    }

    // Backend cuma nyimpen NAMA wilayah (bukan kode BPS), jadi buat nge-restore pilihan dropdown
    // yang udah kesimpen sebelumnya, harus dicari manual by-name: provinsiList yang udah di-fetch
    // di init dipakai buat cari provinsi, baru fetch kota-nya buat cari kota, dst berjenjang.
    // Kalau namanya gak match apa pun di listnya (data lama/beda ejaan), dibiarin null - user
    // tinggal pilih ulang manual, gak bikin crash.
    private suspend fun syncSelectedWilayah(provinsiName: String?, kotaName: String?, kecamatanName: String?) {
        if (provinsiName == null) return
        val provinsi = _uiState.value.provinsiList.find { it.name.equals(provinsiName, ignoreCase = true) } ?: return
        _uiState.update { it.copy(selectedProvinsi = provinsi, isLoadingKota = true) }
        val kotaList = wilayahRepository.getRegencies(provinsi.id).getOrNull() ?: emptyList()
        _uiState.update { it.copy(kotaList = kotaList, isLoadingKota = false) }

        if (kotaName == null) return
        val kota = kotaList.find { it.name.equals(kotaName, ignoreCase = true) } ?: return
        _uiState.update { it.copy(selectedKota = kota, isLoadingKecamatan = true) }
        val kecamatanList = wilayahRepository.getDistricts(kota.id).getOrNull() ?: emptyList()
        _uiState.update { it.copy(kecamatanList = kecamatanList, isLoadingKecamatan = false) }

        if (kecamatanName == null) return
        val kecamatan = kecamatanList.find { it.name.equals(kecamatanName, ignoreCase = true) }
        _uiState.update { it.copy(selectedKecamatan = kecamatan) }
    }

    fun onProvinsiSelected(item: WilayahItem) {
        _uiState.update {
            it.copy(
                selectedProvinsi = item,
                selectedKota = null,
                selectedKecamatan = null,
                kotaList = emptyList(),
                kecamatanList = emptyList(),
                successMessage = null,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingKota = true) }
            wilayahRepository.getRegencies(item.id)
                .onSuccess { list -> _uiState.update { it.copy(kotaList = list, isLoadingKota = false, errorMessage = null) } }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoadingKota = false,
                            errorMessage = "Gagal memuat daftar kota/kabupaten. Pilih ulang provinsinya buat coba lagi."
                        )
                    }
                }
        }
    }

    fun onKotaSelected(item: WilayahItem) {
        _uiState.update {
            it.copy(
                selectedKota = item,
                selectedKecamatan = null,
                kecamatanList = emptyList(),
                successMessage = null,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingKecamatan = true) }
            wilayahRepository.getDistricts(item.id)
                .onSuccess { list -> _uiState.update { it.copy(kecamatanList = list, isLoadingKecamatan = false, errorMessage = null) } }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoadingKecamatan = false,
                            errorMessage = "Gagal memuat daftar kecamatan. Pilih ulang kota/kabupatennya buat coba lagi."
                        )
                    }
                }
        }
    }

    fun onKecamatanSelected(item: WilayahItem) {
        _uiState.update { it.copy(selectedKecamatan = item, successMessage = null) }
    }

    fun onNikInputChange(v: String) = _uiState.update { it.copy(nikInput = v.filter { c -> c.isDigit() }.take(16), successMessage = null) }
    fun onFotoKtpCaptured(uri: Uri) = _uiState.update { it.copy(fotoKtpPreviewUri = uri, successMessage = null, errorMessage = null) }
    fun onNamaLengkapChange(v: String) = _uiState.update { it.copy(namaLengkap = v, successMessage = null) }
    fun onTanggalLahirChange(v: String) = _uiState.update { it.copy(tanggalLahir = v, successMessage = null) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, successMessage = null) }
    fun onNoHpChange(v: String) = _uiState.update { it.copy(noHp = v, successMessage = null) }
    fun onAlamatChange(v: String) = _uiState.update { it.copy(alamat = v, successMessage = null) }
    fun onTipePekerjaanChange(v: TipePekerjaan) = _uiState.update { it.copy(tipePekerjaan = v, successMessage = null) }
    fun onPekerjaanChange(v: String) = _uiState.update { it.copy(pekerjaan = v, successMessage = null) }
    fun onPendapatanChange(v: String) = _uiState.update { it.copy(pendapatanBulanan = v.filter { c -> c.isDigit() }, successMessage = null) }
    fun onNamaBankChange(v: String) = _uiState.update { it.copy(namaBank = v, successMessage = null) }
    fun onNomorRekeningChange(v: String) = _uiState.update { it.copy(nomorRekening = v.filter { c -> c.isDigit() }, successMessage = null) }
    fun onNamaPemilikRekeningChange(v: String) = _uiState.update { it.copy(namaPemilikRekening = v, successMessage = null) }

    fun save() {
        val state = _uiState.value
        if (state.tanggalLahir.isNotBlank() && !Validators.isOldEnough(state.tanggalLahir)) {
            _uiState.update { it.copy(errorMessage = Validators.tanggalLahirError(state.tanggalLahir), successMessage = null) }
            return
        }
        val sendNik = state.nik.isBlank() && state.nikInput.isNotBlank()
        if (sendNik && state.nikInput.length != 16) {
            _uiState.update { it.copy(errorMessage = "NIK harus 16 digit", successMessage = null) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            val fotoBase64 = state.fotoKtpPreviewUri?.let { uri -> encodeKtpPhoto(appContext, uri) }
            if (state.fotoKtpPreviewUri != null && fotoBase64 == null) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Foto KTP gagal dibaca, coba foto ulang") }
                return@launch
            }
            customerRepository.updateMe(
                CustomerUpdateRequest(
                    nik = if (sendNik) state.nikInput else null,
                    fotoKtp = fotoBase64,
                    namaLengkap = state.namaLengkap.ifBlank { null },
                    tanggalLahir = state.tanggalLahir.ifBlank { null },
                    email = state.email.ifBlank { null },
                    noHp = state.noHp.ifBlank { null },
                    alamat = state.alamat.ifBlank { null },
                    tipePekerjaan = state.tipePekerjaan?.apiValue,
                    pekerjaan = state.pekerjaan.ifBlank { null },
                    pendapatanBulanan = state.pendapatanBulanan.toDoubleOrNull(),
                    provinsi = state.selectedProvinsi?.name,
                    kota = state.selectedKota?.name,
                    kecamatan = state.selectedKecamatan?.name,
                    namaBank = state.namaBank.ifBlank { null },
                    nomorRekening = state.nomorRekening.ifBlank { null },
                    namaPemilikRekening = state.namaPemilikRekening.ifBlank { null }
                )
            ).onSuccess { profile ->
                // Sinkron status NIK/foto dari server: NIK yang baru disimpan jadi read-only,
                // preview foto dibuang (udah kesimpen -> "Foto KTP sudah diunggah").
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        successMessage = "Profil berhasil diperbarui",
                        nik = profile.nik ?: it.nik,
                        nikInput = if (profile.nik.isNullOrBlank()) it.nikInput else "",
                        hasFotoKtp = profile.hasFotoKtp,
                        fotoKtpLockReason = profile.fotoKtpLockReason,
                        fotoKtpPreviewUri = null
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false, errorMessage = error.message ?: "Gagal menyimpan profil") }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenDataStore.clearToken()
            _uiState.update { it.copy(loggedOut = true) }
        }
    }
}
