package com.example.sakuku.data.remote

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runs the 2-step token exchange behind "Masuk dengan Google":
 *
 * 1. Credential Manager shows the system account picker and hands back a GOOGLE ID token
 *    (issuer accounts.google.com, audience = the Firebase project's "Web client ID").
 * 2. That Google ID token is exchanged for a FIREBASE ID token via
 *    FirebaseAuth.signInWithCredential() - the backend only accepts the second kind
 *    (CustomerAuthService.googleSignIn() calls the Admin SDK's verifyIdToken(), which rejects a
 *    raw Google token outright because its issuer doesn't match).
 *
 * activityContext MUST be an Activity context (e.g. LocalContext.current from a Composable) -
 * CredentialManager needs it to host the account-picker UI. Passing an application context here
 * throws at runtime, not compile time.
 */
@Singleton
class GoogleSignInClient @Inject constructor() {

    suspend fun signIn(activityContext: Context, webClientId: String): Result<String> {
        return try {
            Log.d(TAG, "step 1: requesting credential from Credential Manager")
            val googleIdOption = GetSignInWithGoogleOption.Builder(webClientId).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(activityContext)
            val result = credentialManager.getCredential(activityContext, request)
            Log.d(TAG, "step 1 OK: got credential of type ${result.credential.type}")

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            Log.d(TAG, "step 2 OK: parsed GoogleIdTokenCredential for ${googleIdTokenCredential.email}")

            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            Log.d(TAG, "step 3: exchanging with FirebaseAuth.signInWithCredential()")
            val authResult = FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await()
            Log.d(TAG, "step 3 OK: firebase user = ${authResult.user?.uid}")
            val firebaseIdToken = authResult.user?.getIdToken(false)?.await()?.token
            Log.d(TAG, "step 4: got firebase ID token = ${firebaseIdToken != null}")

            if (firebaseIdToken != null) {
                Result.success(firebaseIdToken)
            } else {
                Result.failure(Exception("Gagal mendapatkan token dari Firebase"))
            }
        } catch (e: GetCredentialException) {
            // Termasuk kasus user nge-cancel account picker - caller (LoginViewModel) yang
            // nentuin apakah ini ditampilin sebagai error atau didiemin aja. Tetap di-log FULL
            // di sini (bukan cuma message) soalnya UI sengaja nge-null-in error yang keliatan
            // kayak "cancel" - kalau ternyata itu misdetect, ini satu-satunya tempat keliatan.
            Log.e(TAG, "GetCredentialException: type=${e.type}, class=${e.javaClass.simpleName}", e)
            Result.failure(e)
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "GoogleIdTokenParsingException", e)
            Result.failure(Exception("Gagal membaca kredensial Google", e))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected exception: ${e.javaClass.simpleName}", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "GoogleSignInClient"
    }
}
