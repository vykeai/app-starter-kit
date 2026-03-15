package com.onlystack.starterapp.core.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps the AndroidX Biometric library behind a simple two-method interface.
 *
 * Inject this class into a ViewModel or use it directly from an Activity/Fragment.
 * The [authenticate] call must be made from the UI thread with a live
 * [FragmentActivity] reference.
 *
 * Supported authenticators: BIOMETRIC_STRONG | DEVICE_CREDENTIAL
 * (falls back to PIN/pattern/password when no biometric is enrolled).
 */
@Singleton
class BiometricHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val biometricManager = BiometricManager.from(context)

    /**
     * Returns true if the device can authenticate with biometrics or a
     * device credential (PIN / pattern / password).
     */
    fun isAvailable(): Boolean {
        val result = biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Displays the system biometric prompt.
     *
     * @param activity  The foreground [FragmentActivity] required by AndroidX Biometric.
     * @param title     Primary title shown in the prompt dialog.
     * @param subtitle  Secondary text shown beneath the title.
     * @param onSuccess Invoked on the main thread after successful authentication.
     * @param onError   Invoked on the main thread when authentication fails or is cancelled;
     *                  receives a human-readable error description.
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Confirm identity",
        subtitle: String = "Use your biometric credential",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    // errorCode 10 = user cancelled; errorCode 13 = cancelled by system.
                    // Both are treated as a non-fatal dismissal rather than an error.
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_CANCELED
                    ) {
                        onError("Authentication cancelled")
                    } else {
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    // Individual attempt failed; the prompt remains open — do not close.
                }
            },
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()

        prompt.authenticate(promptInfo)
    }
}
