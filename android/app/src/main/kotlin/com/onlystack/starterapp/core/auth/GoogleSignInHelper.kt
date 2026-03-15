package com.onlystack.starterapp.core.auth

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

// Google Sign-In helper
// TODO: Add Google Sign-In dependency to libs.versions.toml and build.gradle.kts:
//   googleSignIn = { group = "com.google.android.gms", name = "play-services-auth", version = "21.0.0" }
// TODO: Get your Web Client ID from Google Cloud Console -> APIs & Services -> Credentials
//   (the "Web application" type OAuth 2.0 client ID, NOT the Android one)

data class GoogleSignInCredential(
    val idToken: String,
    val displayName: String?,
    val email: String?,
)

sealed class GoogleSignInResult {
    data class Success(val credential: GoogleSignInCredential) : GoogleSignInResult()
    object Cancelled : GoogleSignInResult()
    data class Failure(val error: Throwable) : GoogleSignInResult()
}

@Singleton
class GoogleSignInHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    // TODO: Replace with your Web Client ID
    private val webClientId = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"

    /**
     * Launch Google Sign-In.
     * TODO: Uncomment after adding play-services-auth dependency.
     * Usage from Activity:
     *
     *   val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
     *       val signInResult = googleSignInHelper.handleActivityResult(result.resultCode, result.data)
     *       viewModel.onGoogleSignInResult(signInResult)
     *   }
     *   googleSignInHelper.launchSignIn(launcher)
     */
    fun getSignInIntent(): Intent? {
        // TODO: Uncomment after adding SDK:
        /*
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()
        return GoogleSignIn.getClient(context, gso).signInIntent
        */
        return null
    }

    fun handleActivityResult(resultCode: Int, data: Intent?): GoogleSignInResult {
        // TODO: Uncomment after adding SDK:
        /*
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        return try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken ?: return GoogleSignInResult.Failure(Exception("No ID token"))
            GoogleSignInResult.Success(
                GoogleSignInCredential(idToken, account.displayName, account.email)
            )
        } catch (e: ApiException) {
            if (e.statusCode == CommonStatusCodes.CANCELED) GoogleSignInResult.Cancelled
            else GoogleSignInResult.Failure(e)
        }
        */
        return GoogleSignInResult.Failure(UnsupportedOperationException("Google Sign-In SDK not yet added. See GoogleSignInHelper.kt TODO comments."))
    }
}
