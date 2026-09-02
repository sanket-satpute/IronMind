package com.sanket_satpute_20.ironmind.focus

import android.content.Context
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.Task
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class FocusPattern(
    val taskName: String,
    val averageScore: Float,
    val totalRatings: Int,
    val trend: String  // "improving", "declining", "stable"
)

data class DayOfWeekPattern(
    val dayName: String,
    val averageScore: Float,
    val sampleSize: Int
)

object FocusAnalyzer {

    suspend fun getTaskFocusPatterns(context: Context): List<FocusPattern> {
        val dao = IronMindDatabase.getDatabase(context).taskDao()
        val thirtyDaysAgo = LocalDate.now().minusDays(30).toString()
        val tasks = dao.getTasksWithFocusScores(thirtyDaysAgo)

        return tasks
            .groupBy { it.name }
            .map { (taskName, taskList) ->
                val avg = taskList.map { it.focusScore }.average().toFloat()
                val trend = calculateTrend(taskList)
                FocusPattern(
                    taskName = taskName,
                    averageScore = avg,
                    totalRatings = taskList.size,
                    trend = trend
                )
            }
            .sortedByDescending { it.averageScore }
    }

    suspend fun getDayOfWeekPatterns(context: Context): List<DayOfWeekPattern> {
        val dao = IronMindDatabase.getDatabase(context).taskDao()
        val thirtyDaysAgo = LocalDate.now().minusDays(30).toString()
        val tasks = dao.getTasksWithFocusScores(thirtyDaysAgo)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val dayNames = listOf("Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday", "Sunday")

        return dayNames.mapNotNull { dayName ->
            val dayTasks = tasks.filter { task ->
                runCatching {
                    LocalDate.parse(task.date, formatter)
                        .dayOfWeek.name
                        .lowercase()
                        .replaceFirstChar { it.uppercase() } == dayName
                }.getOrElse { false }
            }

            if (dayTasks.isEmpty()) return@mapNotNull null

            DayOfWeekPattern(
                dayName = dayName,
                averageScore = dayTasks.map { it.focusScore }.average().toFloat(),
                sampleSize = dayTasks.size
            )
        }
    }

    suspend fun getLast30DaysFocusData(context: Context): List<Pair<String, Float>> {
        val dao = IronMindDatabase.getDatabase(context).taskDao()
        val thirtyDaysAgo = LocalDate.now().minusDays(30).toString()
        val tasks = dao.getTasksWithFocusScores(thirtyDaysAgo)

        // Average score per day
        return tasks
            .groupBy { it.date }
            .map { (date, dayTasks) ->
                date to dayTasks.map { it.focusScore }.average().toFloat()
            }
            .sortedBy { it.first }
    }

    private fun calculateTrend(tasks: List<Task>): String {
        if (tasks.size < 4) return "stable"
        val sorted = tasks.sortedBy { it.date }
        val firstHalf = sorted.take(sorted.size / 2).map { it.focusScore }.average()
        val secondHalf = sorted.drop(sorted.size / 2).map { it.focusScore }.average()
        return when {
            secondHalf - firstHalf > 0.5 -> "improving"
            firstHalf - secondHalf > 0.5 -> "declining"
            else -> "stable"
        }
    }
}