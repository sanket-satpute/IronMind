package com.sanket_satpute_20.ironmind.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.SavedTaskBlueprintDao
import com.sanket_satpute_20.ironmind.data.TaskDao
import com.sanket_satpute_20.ironmind.data.TaskEventDao

class TaskBuilderViewModelFactory(
    private val taskDao: TaskDao,
    private val taskEventDao: TaskEventDao,
    private val savedTaskBlueprintDao: SavedTaskBlueprintDao,
    private val prefManager: PrefManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskBuilderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskBuilderViewModel(taskDao, taskEventDao, savedTaskBlueprintDao, prefManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
