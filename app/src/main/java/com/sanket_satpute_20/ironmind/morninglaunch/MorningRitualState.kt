package com.sanket_satpute_20.ironmind.morninglaunch

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents the distinct stages of the morning ritual.
 * Parcelable so it can be saved in SavedStateHandle.
 */
@Parcelize
enum class RitualStep : Parcelable {
    WAKE_ENTRY,
    YESTERDAY_RECAP,
    TASK_COMMITMENT,
    MODE_RECOMMENDATION,
    MANUAL_OVERRIDE,
    OATH_HOLD,
    SHIELD_REASSEMBLY,
    MAIN_FLOW_HANDOFF // Hand off to the legacy Monk/Warrior flows
}

@Parcelize
data class MorningRitualState(
    val currentStep: RitualStep = RitualStep.WAKE_ENTRY,
    val selectedTaskIds: List<Long> = emptyList(),
    val recommendedMode: String? = null,
    val recommendationReason: String? = null,
    val confirmedMode: String? = null,
    val isOathCompleted: Boolean = false
) : Parcelable
