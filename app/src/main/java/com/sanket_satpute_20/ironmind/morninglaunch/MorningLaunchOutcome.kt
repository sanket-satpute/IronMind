package com.sanket_satpute_20.ironmind.morninglaunch

enum class MorningLaunchOutcome {
    PENDING,
    STARTED_IMMEDIATELY,
    STARTED_AFTER_DELAY,
    REMIND_5,
    SKIPPED,
    ABANDONED,
    COMPLETED;

    companion object {
        fun fromWireValue(value: String): MorningLaunchOutcome =
            entries.firstOrNull { it.name == value } ?: PENDING
    }
}
