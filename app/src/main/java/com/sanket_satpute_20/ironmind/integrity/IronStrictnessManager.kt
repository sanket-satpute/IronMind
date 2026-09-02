package com.sanket_satpute_20.ironmind.integrity

import android.content.Context
import com.sanket_satpute_20.ironmind.data.PrefManager
import java.time.LocalDate

data class IronStrictnessProfile(
    val activeToday: Boolean,
    val punishmentSecondsBonus: Long,
    val workLockPenaltyBonus: Int,
    val emergencyExitReduction: Int,
    val requireCleanEarnedUnlock: Boolean
)

class IronStrictnessManager(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = PrefManager.getInstance(appContext)

    fun isActiveToday(today: LocalDate = LocalDate.now()): Boolean {
        return prefs.ironIntegrityStrictnessBoostActive &&
            prefs.ironIntegrityStrictnessBoostDate == today.toString()
    }

    fun getTodayProfile(today: LocalDate = LocalDate.now()): IronStrictnessProfile {
        if (!isActiveToday(today)) {
            return IronStrictnessProfile(
                activeToday = false,
                punishmentSecondsBonus = 0L,
                workLockPenaltyBonus = 0,
                emergencyExitReduction = 0,
                requireCleanEarnedUnlock = false
            )
        }

        return IronStrictnessProfile(
            activeToday = true,
            punishmentSecondsBonus = prefs.ironIntegrityStrictnessBoostSeconds,
            workLockPenaltyBonus = prefs.ironIntegrityStrictnessBoostWorkLockPenaltyBonus,
            emergencyExitReduction = prefs.ironIntegrityStrictnessBoostEmergencyExitReduction,
            requireCleanEarnedUnlock = prefs.ironIntegrityStrictnessBoostRequireCleanEarnedUnlock
        )
    }
}
