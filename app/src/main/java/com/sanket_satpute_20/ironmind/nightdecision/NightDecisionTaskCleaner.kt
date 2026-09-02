package com.sanket_satpute_20.ironmind.nightdecision

import android.content.Context
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.TaskEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

internal suspend fun clearNightDecisionTasksWithHistory(
    context: Context,
    targetDate: LocalDate,
    reason: String
) {
    withContext(Dispatchers.IO) {
        val db = IronMindDatabase.getDatabase(context)
        val taskDao = db.taskDao()
        val taskEventDao = db.taskEventDao()
        val date = targetDate.toString()
        val tasks = taskDao.getTasksForDateOnce(date)

        tasks.forEach { task ->
            taskEventDao.insert(
                TaskEvent(
                    taskId = task.id,
                    taskName = task.name,
                    date = task.date,
                    eventType = "DELETED",
                    timestamp = System.currentTimeMillis(),
                    oldStartTime = task.startTime,
                    oldEndTime = task.endTime,
                    reason = reason
                )
            )
            taskDao.deleteTaskById(task.id)
        }
    }
}
