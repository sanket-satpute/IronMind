package com.sanket_satpute_20.ironmind.focus

data class PomodoroSessionConfig(
    val sessionId: String,
    val source: PomodoroSessionSource,
    val taskId: Int? = null,
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val totalDurationMinutes: Int = 0,
    val preset: PomodoroPreset,
    val sessionHardEndsAt: Long = 0L
)

enum class PomodoroSessionSource {
    TASK,
    HOME;

    companion object {
        fun fromWireValue(value: String): PomodoroSessionSource {
            return runCatching { valueOf(value) }.getOrDefault(TASK)
        }
    }
}
