package com.sanket_satpute_20.ironmind.nightdecision

enum class NightDecisionStatus {
    UNDECIDED,
    PLANNED,
    HOLIDAY,
    EMERGENCY_LEAVE;

    companion object {
        fun fromWireValue(value: String): NightDecisionStatus {
            return entries.firstOrNull { it.name == value } ?: UNDECIDED
        }
    }
}
