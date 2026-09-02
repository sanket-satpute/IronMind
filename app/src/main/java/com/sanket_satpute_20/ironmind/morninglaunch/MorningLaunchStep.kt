package com.sanket_satpute_20.ironmind.morninglaunch

import com.sanket_satpute_20.ironmind.challenge.MorningTask
import com.sanket_satpute_20.ironmind.challenge.MorningTaskType
import com.sanket_satpute_20.ironmind.challenge.RoutineBuilder

data class MorningLaunchStep(
    val index: Int,
    val type: MorningTaskType,
    val title: String,
    val durationSeconds: Int,
    val instruction: String,
    val hasTimer: Boolean
) {
    companion object {
        fun fromTask(index: Int, task: MorningTask): MorningLaunchStep {
            return MorningLaunchStep(
                index = index,
                type = task.type,
                title = task.title,
                durationSeconds = task.durationSeconds,
                instruction = task.instruction,
                hasTimer = task.hasTimer
            )
        }

        fun forMode(mode: MorningLaunchMode): List<MorningLaunchStep> {
            return RoutineBuilder.getTasksForRoutine(mode.toRoutineType())
                .mapIndexed { index, task -> fromTask(index, task) }
        }
    }
}
