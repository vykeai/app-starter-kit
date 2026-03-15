package com.onlystack.starterapp.nfr

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs = context.getSharedPreferences("review_manager", Context.MODE_PRIVATE)
    private val minimumDaysBetweenRequests = 7L

    fun requestReviewIfEligible(activity: Activity) {
        val lastRequest = prefs.getLong("last_request_timestamp", 0L)
        val now = System.currentTimeMillis()
        val minInterval = minimumDaysBetweenRequests * 24 * 60 * 60 * 1000

        if (now - lastRequest < minInterval) return

        prefs.edit().putLong("last_request_timestamp", now).apply()

        val manager = ReviewManagerFactory.create(context)
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                manager.launchReviewFlow(activity, task.result)
            }
        }
    }
}
