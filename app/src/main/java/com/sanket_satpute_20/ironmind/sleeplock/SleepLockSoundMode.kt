package com.sanket_satpute_20.ironmind.sleeplock

enum class SleepLockSoundMode {
    SINGLE,
    BLEND,
    ROTATE;

    companion object {
        fun fromWireValue(value: String): SleepLockSoundMode =
            entries.firstOrNull { it.name == value } ?: SINGLE
    }
}
