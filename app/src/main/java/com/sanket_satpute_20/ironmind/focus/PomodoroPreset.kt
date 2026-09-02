package com.sanket_satpute_20.ironmind.focus

enum class PomodoroPreset(
    val workMinutes: Int,
    val shortBreakMinutes: Int,
    val longBreakMinutes: Int,
    val cyclesBeforeLongBreak: Int
) {
    CLASSIC_25_5(
        workMinutes = 25,
        shortBreakMinutes = 5,
        longBreakMinutes = 30,
        cyclesBeforeLongBreak = 4
    ),
    DEEP_50_10(
        workMinutes = 50,
        shortBreakMinutes = 10,
        longBreakMinutes = 30,
        cyclesBeforeLongBreak = 4
    ),
    QUICK_15_5(
        workMinutes = 15,
        shortBreakMinutes = 5,
        longBreakMinutes = 20,
        cyclesBeforeLongBreak = 4
    );

    companion object {
        fun fromWireValue(value: String): PomodoroPreset {
            return runCatching { valueOf(value) }.getOrDefault(CLASSIC_25_5)
        }
    }
}
