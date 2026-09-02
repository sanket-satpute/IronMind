package com.sanket_satpute_20.ironmind.focus

enum class WorkLockStage {
    IDLE,
    ACTIVE,
    BROKEN,
    COMPLETED,
    EXPIRED;

    companion object {
        fun fromWireValue(value: String): WorkLockStage {
            return entries.firstOrNull { it.name == value } ?: IDLE
        }
    }
}
