package com.sanket_satpute_20.ironmind.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsManager {
    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context.applicationContext)
        }
    }

    private fun logEvent(eventName: String, params: Bundle? = null) {
        firebaseAnalytics?.logEvent(eventName, params)
    }

    fun logMorningRitualCompleted() {
        logEvent("morning_ritual_completed")
    }

    fun logFirstBlockExperienced(appName: String) {
        val bundle = Bundle().apply {
            putString("app_name", appName)
        }
        logEvent("first_block_experienced", bundle)
    }

    fun logRoughRecapViewed() {
        logEvent("rough_recap_viewed")
    }

    fun logProfileRevealed() {
        logEvent("profile_revealed")
    }

    fun logRelapseReentry(timeSinceFailureMs: Long) {
        val bundle = Bundle().apply {
            putLong("time_since_failure_ms", timeSinceFailureMs)
        }
        logEvent("relapse_reentry", bundle)
    }

    fun logRecommendationAction(mode: String, accepted: Boolean) {
        val bundle = Bundle().apply {
            putString("mode", mode)
            putBoolean("accepted", accepted)
        }
        logEvent(if (accepted) "recommendation_accepted" else "recommendation_rejected", bundle)
    }

    fun logPermissionResult(permissionType: String, granted: Boolean) {
        val bundle = Bundle().apply {
            putString("permission_type", permissionType)
            putBoolean("granted", granted)
        }
        logEvent("permission_result", bundle)
    }

    fun logPermissionRequested(permissionType: String) {
        val bundle = Bundle().apply {
            putString("permission_type", permissionType)
        }
        logEvent("permission_requested", bundle)
    }

    fun logModeChanged(newMode: String) {
        val bundle = Bundle().apply {
            putString("new_mode", newMode)
        }
        logEvent("mode_changed", bundle)
    }

    // ── Phase 6: Beta Instrumentation Events ─────────────────────────────────

    /** Fired once on first ever app open. Identifies this device as a beta participant. */
    fun logBetaUserTagged(userType: String) {
        val bundle = Bundle().apply {
            putString("user_type", userType)
        }
        logEvent("beta_user_tagged", bundle)
        firebaseAnalytics?.setUserProperty("user_type", userType.take(36))
    }

    /** Fired once the user is still using the app ≥1 day after install. Measures D1 retention. */
    fun logD1Retention() {
        logEvent("d1_retention")
    }

    /** Fired once the user is still using the app ≥7 days after install. Measures D7 retention. */
    fun logD7Retention() {
        logEvent("d7_retention")
    }

    /** Fired at the end of the onboarding flow.
     *  @param durationMs total time (ms) from first launch to onboarding completion. */
    fun logOnboardingCompleted(durationMs: Long) {
        val bundle = Bundle().apply {
            putLong("duration_ms", durationMs)
        }
        logEvent("onboarding_completed", bundle)
    }

    /** Fired when user taps "NOT NOW" or back on a PermissionRationaleScreen.
     *  @param step the permission type they abandoned (e.g. "ACCESSIBILITY"). */
    fun logPermissionFunnelAbandoned(step: String) {
        val bundle = Bundle().apply {
            putString("step", step)
        }
        logEvent("permission_funnel_abandoned", bundle)
    }

    /** Unified morning ritual outcome tracker.
     *  @param outcome one of "COMPLETED", "SKIPPED", "DELAYED" */
    fun logMorningRitualOutcome(outcome: String) {
        val bundle = Bundle().apply {
            putString("outcome", outcome)
        }
        logEvent("morning_ritual_outcome", bundle)
    }

    /** Fired when the mode is first switched to RECOVERY. */
    fun logRecoveryModeEntered() {
        logEvent("recovery_mode_entered")
    }

    /** Fired when the user leaves RECOVERY mode.
     *  @param daysInRecovery number of days spent in Recovery Mode. */
    fun logRecoveryModeExited(daysInRecovery: Int) {
        val bundle = Bundle().apply {
            putInt("days_in_recovery", daysInRecovery)
        }
        logEvent("recovery_mode_exited", bundle)
    }

    // ── Phase 7: Play Billing Events ─────────────────────────────────────────

    /** Fired when the user taps the "Upgrade" button on PremiumUpgradeScreen. */
    fun logPurchaseAttempted() {
        logEvent("purchase_attempted")
    }

    /** Fired after a successful purchase is acknowledged by Play Billing. */
    fun logPurchaseSucceeded() {
        logEvent("purchase_succeeded")
    }

    /**
     * Fired when the billing flow ends with an error or cancellation.
     * @param errorCode the BillingResponseCode from Play Billing.
     */
    fun logPurchaseFailed(errorCode: Int) {
        val bundle = Bundle().apply {
            putInt("error_code", errorCode)
        }
        logEvent("purchase_failed", bundle)
    }

    /**
     * Fired after the user taps "Restore Purchases".
     * @param found true if an active subscription was found and restored.
     */
    fun logRestorePurchasesResult(found: Boolean) {
        val bundle = Bundle().apply {
            putBoolean("found", found)
        }
        logEvent("restore_purchases_result", bundle)
    }
}
