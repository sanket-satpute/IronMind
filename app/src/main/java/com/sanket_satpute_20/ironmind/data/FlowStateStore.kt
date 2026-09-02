package com.sanket_satpute_20.ironmind.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FlowStateStore private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("ironmind_flow_states", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var INSTANCE: FlowStateStore? = null

        fun getInstance(context: Context): FlowStateStore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FlowStateStore(context).also { INSTANCE = it }
            }
        }

        private const val KEY_MORNING_LAUNCH_STEP = "morning_launch_step"
        private const val KEY_MORNING_LAUNCH_DATE = "morning_launch_date"
        private const val KEY_NIGHT_FLOW_STEP = "night_flow_step"
        private const val KEY_NIGHT_FLOW_DATE = "night_flow_date"
    }

    // --- Morning Launch State ---
    fun saveMorningLaunchStep(stepIndex: Int, date: String) {
        prefs.edit {
            putInt(KEY_MORNING_LAUNCH_STEP, stepIndex)
            putString(KEY_MORNING_LAUNCH_DATE, date)
        }
    }

    fun getMorningLaunchStep(currentDate: String): Int {
        val savedDate = prefs.getString(KEY_MORNING_LAUNCH_DATE, "")
        return if (savedDate == currentDate) {
            prefs.getInt(KEY_MORNING_LAUNCH_STEP, 0)
        } else {
            0 // Reset if it's a new day
        }
    }

    fun clearMorningLaunchState() {
        prefs.edit {
            remove(KEY_MORNING_LAUNCH_STEP)
            remove(KEY_MORNING_LAUNCH_DATE)
        }
    }

    // --- Night Flow State ---
    fun saveNightFlowStep(stepIndex: Int, date: String) {
        prefs.edit {
            putInt(KEY_NIGHT_FLOW_STEP, stepIndex)
            putString(KEY_NIGHT_FLOW_DATE, date)
        }
    }

    fun getNightFlowStep(currentDate: String): Int {
        val savedDate = prefs.getString(KEY_NIGHT_FLOW_DATE, "")
        return if (savedDate == currentDate) {
            prefs.getInt(KEY_NIGHT_FLOW_STEP, 0)
        } else {
            0
        }
    }

    fun clearNightFlowState() {
        prefs.edit {
            remove(KEY_NIGHT_FLOW_STEP)
            remove(KEY_NIGHT_FLOW_DATE)
        }
    }
}
