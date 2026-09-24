package com.example.sakuku.service

import android.util.Log
import com.example.sakuku.core.notification.AppNotification
import com.example.sakuku.core.notification.AppNotifier
import com.example.sakuku.core.notification.NotificationChannelType
import com.example.sakuku.data.local.TokenDataStore
import com.example.sakuku.data.repository.CustomerRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TOPIC_PREFIX = "/topics/"

@AndroidEntryPoint
class PushMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notifier: AppNotifier

    @Inject
    lateinit var customerRepository: CustomerRepository

    @Inject
    lateinit var tokenDataStore: TokenDataStore

    // Service Android (bukan lifecycle Compose), jadi butuh scope sendiri buat coroutine -
    // SupervisorJob biar 1 gagal (mis. request network pas belum login) gak mati-in scope-nya
    // buat panggilan berikutnya.
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // onRegistered, BUKAN onNewToken - dicek langsung via javap ke firebase-messaging-25.1.2.aar:
    // onNewToken() ternyata @Deprecated di versi ini, onRegistered() penggantinya (dipicu action
    // broadcast internal FCM_REGISTERED). Dua-duanya ambil value dari intent extra "token" yang
    // SAMA (dicek dari bytecode dispatcher-nya) - jadi walau parameter di sini namanya
    // "installationId", isinya tetap FCM registration token asli, valid dipakai buat
    // Message.setToken() pas backend ngirim push. Ini juga cocok sama meta-data manifest
    // (firebase_messaging_installation_id_enabled=true) yang emang udah nunjuk ke flow ini.
    //
    // Token dicache dulu ke TokenDataStore (app ini guest-access, bisa dapet token SEBELUM
    // login) + langsung dicoba kirim best-effort (bisa gagal kalau belum ada sesi, gapapa -
    // disinkron ulang abis login sukses, lihat LoginViewModel/RegisterViewModel).
    override fun onRegistered(installationId: String) {
        Log.d("INI_TOKEN", installationId)
        serviceScope.launch {
            tokenDataStore.saveFcmToken(installationId)
            customerRepository.updateFcmToken(installationId)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val title = message.notification?.title ?: data[KEY_TITLE] ?: return
        val body = message.notification?.body ?: data[KEY_BODY].orEmpty()

        notifier.show(
            AppNotification(
                title = title,
                body = body,
                channel = NotificationChannelType.fromId(data[KEY_CHANNEL] ?: message.topic()),
                deepLink = data[KEY_DEEP_LINK],
            )
        )
    }

    /** Nama topic bila pesan dikirim ke topic, null bila dikirim ke perangkat. */
    private fun RemoteMessage.topic(): String? =
        from?.takeIf { it.startsWith(TOPIC_PREFIX) }?.removePrefix(TOPIC_PREFIX)

    private companion object {
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_CHANNEL = "channel"
        const val KEY_DEEP_LINK = "deeplink"
    }
}
