package com.sanket_satpute_20.ironmind.morninglaunch

import com.sanket_satpute_20.ironmind.challenge.RoutineType

enum class MorningLaunchMode(
    val label: String,
    val subtitle: String
) {
    MONK(
        label = "Monk",
        subtitle = "Breathing, meditation, journaling."
    ),
    WARRIOR(
        label = "Warrior",
        subtitle = "Activation, discipline, physical momentum."
    ),
    BALANCED(
        label = "Balanced",
        subtitle = "Grounding, stability, practical intent."
    );

    fun toRoutineType(): RoutineType = when (this) {
        MONK -> RoutineType.MONK
        WARRIOR -> RoutineType.WARRIOR
        BALANCED -> RoutineType.BALANCED
    }

    companion object {
        fun fromRoutineType(type: RoutineType): MorningLaunchMode = when (type) {
            RoutineType.MONK -> MONK
            RoutineType.WARRIOR -> WARRIOR
            RoutineType.BALANCED -> BALANCED
        }

        fun fromWireValue(value: String): MorningLaunchMode =
            entries.firstOrNull { it.name == value } ?: MONK
    }
}
