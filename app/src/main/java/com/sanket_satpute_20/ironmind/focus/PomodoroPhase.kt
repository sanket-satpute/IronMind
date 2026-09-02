package com.sanket_satpute_20.ironmind.focus

enum class PomodoroPhase {
    IDLE,
    WORK,
    SHORT_BREAK,
    LONG_BREAK,
    COMPLETED,
    BROKEN;

    companion object {
        fun fromWireValue(value: String): PomodoroPhase {
            return runCatching { valueOf(value) }.getOrDefault(IDLE)
        }
    }
}
