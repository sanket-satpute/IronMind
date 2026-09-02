package com.sanket_satpute_20.ironmind.focus

data class PomodoroSessionState(
    val active: Boolean,
    val sessionId: String,
    val source: PomodoroSessionSource,
    val taskId: Int,
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val totalDurationMinutes: Int,
    val preset: PomodoroPreset,
    val phase: PomodoroPhase,
    val phaseEndsAt: Long,
    val sessionHardEndsAt: Long,
    val intervalIndex: Int,
    val completedWorkIntervals: Int,
    val breachCount: Int,
    val breakUnlocked: Boolean,
    val startedAt: Long
) {
    val isBreakPhase: Boolean
        get() = phase == PomodoroPhase.SHORT_BREAK || phase == PomodoroPhase.LONG_BREAK
}
