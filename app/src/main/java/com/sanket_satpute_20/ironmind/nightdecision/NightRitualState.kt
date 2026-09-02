package com.sanket_satpute_20.ironmind.nightdecision

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class NightRitualStep {
    SHIELD_RECAP,
    REFLECTION,
    RECOVERY_RECOMMENDATION,
    PLANNING,
    HANDOFF
}

sealed class NightRitualState : Parcelable {
    @Parcelize
    data class Loading(val date: String) : NightRitualState()

    @Parcelize
    data class Active(
        val date: String,
        val currentStep: NightRitualStep,
        val integrityScore: Float = 1.0f,
        val isRoughDay: Boolean = false,
        val strugglingDays: Int = 0,
        val reflectionText: String = "",
        val sleepLockEnabled: Boolean = false
    ) : NightRitualState()

    @Parcelize
    data class Finished(val date: String) : NightRitualState()
}
