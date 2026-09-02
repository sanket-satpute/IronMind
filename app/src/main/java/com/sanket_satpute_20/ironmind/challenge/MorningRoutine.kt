package com.sanket_satpute_20.ironmind.challenge

enum class RoutineType { MONK, WARRIOR, BALANCED }

enum class MorningTaskType {
    BREATHING,      // Monk, Balanced
    MEDITATION,     // Monk, Balanced
    JOURNALING,     // Monk
    RUN,            // Warrior, Balanced
    PUSHUPS,        // Warrior
    COLD_SHOWER,    // Warrior
    GRATITUDE,      // Balanced
    STRETCHING      // Balanced
}

data class MorningTask(
    val type: MorningTaskType,
    val title: String,
    val durationSeconds: Int,   // how long the task takes
    val instruction: String,    // what to do
    val hasTimer: Boolean       // show countdown or just completion button
)

object RoutineBuilder {
    fun getTasksForRoutine(type: RoutineType): List<MorningTask> {
        return when (type) {
            RoutineType.MONK -> listOf(
                MorningTask(MorningTaskType.BREATHING, "Box Breathing",
                    240, "4 counts in. 4 hold. 4 out. 4 hold.", true),
                MorningTask(MorningTaskType.MEDITATION, "Silent Meditation",
                    300, "Sit still. No phone. Eyes closed.", true),
                MorningTask(MorningTaskType.JOURNALING, "Morning Journal",
                    0, "Write 3 things you will accomplish today.", false)
            )
            RoutineType.WARRIOR -> listOf(
                MorningTask(MorningTaskType.PUSHUPS, "20 Pushups",
                    0, "Drop and do 20. No excuses.", false),
                MorningTask(MorningTaskType.RUN, "Morning Run",
                    0, "Minimum 10 minutes. Outside.", false),
                MorningTask(MorningTaskType.COLD_SHOWER, "Cold Shower",
                    120, "2 minutes. Full cold. No warm up.", true)
            )
            RoutineType.BALANCED -> listOf(
                MorningTask(MorningTaskType.BREATHING, "Box Breathing",
                    180, "4 counts in. 4 hold. 4 out. 4 hold.", true),
                MorningTask(MorningTaskType.STRETCHING, "Full Body Stretch",
                    300, "5 minutes. Every major muscle group.", true),
                MorningTask(MorningTaskType.GRATITUDE, "Gratitude Practice",
                    0, "Say 3 things out loud you are grateful for.", false)
            )
        }
    }
}
