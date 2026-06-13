package com.websbaba.nitigrow.core.biometric

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

sealed interface BiometricResult {
    data object Success : BiometricResult
    data class Failed(val reason: String) : BiometricResult
    data object Unavailable : BiometricResult
}

object BiometricAuthenticator {

    fun isAvailable(activity: FragmentActivity): Boolean {
        val mgr = BiometricManager.from(activity)
        val flags = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        return mgr.canAuthenticate(flags) == BiometricManager.BIOMETRIC_SUCCESS
    }

    suspend fun prompt(
        activity: FragmentActivity,
        title: String = "Unlock NitiGrow",
        subtitle: String = "Use biometric to continue"
    ): BiometricResult = suspendCancellableCoroutine { cont ->
        if (!isAvailable(activity)) {
            cont.resume(BiometricResult.Unavailable); return@suspendCancellableCoroutine
        }
        val executor = androidx.core.content.ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                if (cont.isActive) cont.resume(BiometricResult.Success)
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (cont.isActive) cont.resume(BiometricResult.Failed(errString.toString()))
            }
            override fun onAuthenticationFailed() {
                // user retries automatically; ignore
            }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
        prompt.authenticate(info)
    }
}
