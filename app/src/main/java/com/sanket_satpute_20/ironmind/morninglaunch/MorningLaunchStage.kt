package com.sanket_satpute_20.ironmind.morninglaunch

enum class MorningLaunchStage {
    IDLE,
    PROMPTING,
    SNOOZED,
    IN_PROGRESS,
    COMPLETED,
    SKIPPED;

    companion object {
        fun fromWireValue(value: String): MorningLaunchStage =
            entries.firstOrNull { it.name == value } ?: IDLE
    }
}
