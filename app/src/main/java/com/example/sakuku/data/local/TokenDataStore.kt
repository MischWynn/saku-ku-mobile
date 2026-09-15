package com.example.sakuku.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sakuku_session")
private val KEY_TOKEN = stringPreferencesKey("auth_token")
private val KEY_REMEMBERED_IDENTIFIER = stringPreferencesKey("remembered_identifier")

// Sesi login (token JWT) - data kecil sejenis pengaturan, makanya DataStore, BUKAN Room.
// Room dipakai buat data koleksi/list yang perlu di-cache (riwayat pengajuan, dst) - beda
// kebutuhan, lihat CLAUDE.md project.
@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs -> prefs[KEY_TOKEN] }

    suspend fun getTokenOnce(): String? = tokenFlow.first()

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs -> prefs[KEY_TOKEN] = token }
    }

    suspend fun clearToken() {
        context.dataStore.edit { prefs -> prefs.remove(KEY_TOKEN) }
    }

    // "Ingat username" - simpen identifier (email/no HP) doang, BUKAN password. Null berarti
    // gak diinget (checkbox gak dicentang, atau abis di-uncheck).
    suspend fun getRememberedIdentifier(): String? = context.dataStore.data.map { it[KEY_REMEMBERED_IDENTIFIER] }.first()

    suspend fun saveRememberedIdentifier(identifier: String?) {
        context.dataStore.edit { prefs ->
            if (identifier != null) prefs[KEY_REMEMBERED_IDENTIFIER] = identifier
            else prefs.remove(KEY_REMEMBERED_IDENTIFIER)
        }
    }
}
