package com.sanket_satpute_20.ironmind.autopsy

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.provider.Settings
import com.sanket_satpute_20.ironmind.apps.AppClassificationRepository
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import java.util.Calendar

data class AppUsageData(
    val packageName: String,
    val appName: String,
    val totalTimeMs: Long,
    val isPotentiallyWorkRelated: Boolean = false,
    val isAlreadyBlocked: Boolean = false
)

data class DailyAutopsyResult(
    val totalWastedMs: Long,
    val totalInvestedMs: Long,
    val topOffenders: List<AppUsageData>,
    val verdict: String,
    val wastedPercent: Float,
    val investedPercent: Float
)

data class MonthlyAuditResult(
    val totalWastedMs: Long,
    val totalInvestedMs: Long,
    val topOffenders: List<AppUsageData>,
    val appWiseUsage: List<AppUsageData>,
    val wastedPercent: Float,
    val investedPercent: Float,
    val auditVerdict: String
)

object UsageAnalyzer {

    enum class PermissionState {
        GRANTED,
        DENIED,
        NOT_CHECKED
    }

    private val BROWSERS = setOf(
        "com.android.chrome", "com.brave.browser", "org.mozilla.firefox",
        "com.opera.browser", "com.sec.android.app.sbrowser"
    )

    fun getPermissionState(context: Context): PermissionState {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return if (mode == AppOpsManager.MODE_ALLOWED) PermissionState.GRANTED
        else PermissionState.DENIED
    }

    fun openUsageAccessSettings(context: Context) {
        context.startActivity(
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    suspend fun getYesterdayAutopsy(context: Context): DailyAutopsyResult? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endTime = calendar.timeInMillis

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )

        if (usageStats.isNullOrEmpty()) return null

        val blockedApps = AppClassificationRepository.getInstance(context).getBlockedLikePackagesSnapshot()
        val pm = context.packageManager
        
        val workApps = getWorkRelatedApps(context)

        val aggregatedStats = usageStats.groupBy { it.packageName }
            .mapValues { entry -> entry.value.sumOf { it.totalTimeInForeground } }
            .filter { it.value > 0 && it.key != context.packageName }

        val wastedAppsMap = mutableMapOf<String, AppUsageData>()
        var totalWasted = 0L
        var totalInvested = 0L

        aggregatedStats.forEach { (pkg, timeMs) ->
            val isAlreadyBlocked = pkg in blockedApps || pkg in BROWSERS
            val isWorkRelated = pkg in workApps
            val isSystem = isSystemApp(context, pkg)
            
            val isToxicWaste = !isSystem && !isWorkRelated && !isAlreadyBlocked

            if (isAlreadyBlocked || isToxicWaste) {
                totalWasted += timeMs
                val appName = runCatching {
                    pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                }.getOrElse { pkg }
                
                wastedAppsMap[pkg] = AppUsageData(
                    packageName = pkg, 
                    appName = appName, 
                    totalTimeMs = timeMs, 
                    isPotentiallyWorkRelated = isWorkRelated,
                    isAlreadyBlocked = isAlreadyBlocked
                )
            } else {
                totalInvested += timeMs
            }
        }

        val topOffenders = wastedAppsMap.values
            .sortedByDescending { it.totalTimeMs }
            .take(5)

        val total = totalWasted + totalInvested
        val wastedPercent = if (total > 0) (totalWasted * 100f / total) else 0f
        val investedPercent = 100f - wastedPercent

        return DailyAutopsyResult(
            totalWastedMs = totalWasted,
            totalInvestedMs = totalInvested,
            topOffenders = topOffenders,
            verdict = generateVerdict(wastedPercent, topOffenders),
            wastedPercent = wastedPercent,
            investedPercent = investedPercent
        )
    }

    suspend fun getMonthlyAudit(context: Context): MonthlyAuditResult? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_MONTHLY, startTime, endTime
        )

        if (usageStats.isNullOrEmpty()) return null

        val blockedApps = AppClassificationRepository.getInstance(context).getBlockedLikePackagesSnapshot()
        val pm = context.packageManager
        val workApps = getWorkRelatedApps(context)

        val aggregatedStats = usageStats.groupBy { it.packageName }
            .mapValues { entry -> entry.value.sumOf { it.totalTimeInForeground } }
            .filter { it.value > 0 && it.key != context.packageName }

        val allAppData = mutableListOf<AppUsageData>()
        var totalWasted = 0L
        var totalInvested = 0L

        aggregatedStats.forEach { (pkg, timeMs) ->
            val isAlreadyBlocked = pkg in blockedApps || pkg in BROWSERS
            val isWorkRelated = pkg in workApps
            val isSystem = isSystemApp(context, pkg)
            val isToxicWaste = !isSystem && !isWorkRelated && !isAlreadyBlocked

            val appName = runCatching {
                pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
            }.getOrElse { pkg }

            val data = AppUsageData(
                packageName = pkg,
                appName = appName,
                totalTimeMs = timeMs,
                isPotentiallyWorkRelated = isWorkRelated,
                isAlreadyBlocked = isAlreadyBlocked
            )

            if (isAlreadyBlocked || isToxicWaste) {
                totalWasted += timeMs
            } else {
                totalInvested += timeMs
            }
            allAppData.add(data)
        }

        val sortedUsage = allAppData.sortedByDescending { it.totalTimeMs }
        val total = totalWasted + totalInvested
        val wastedPercent = if (total > 0) (totalWasted * 100f / total) else 0f
        
        return MonthlyAuditResult(
            totalWastedMs = totalWasted,
            totalInvestedMs = totalInvested,
            topOffenders = sortedUsage.filter { it.isAlreadyBlocked || (!isSystemApp(context, it.packageName) && !it.isPotentiallyWorkRelated) }.take(5),
            appWiseUsage = sortedUsage,
            wastedPercent = wastedPercent,
            investedPercent = 100f - wastedPercent,
            auditVerdict = generateMonthlyVerdict(wastedPercent, totalWasted)
        )
    }

    private fun generateMonthlyVerdict(wastedPercent: Float, wastedMs: Long): String {
        val wastedHours = wastedMs / 1000 / 3600
        return when {
            wastedPercent > 60 -> "Catastrophic Leak. You lost $wastedHours hours this month to digital noise. Your identity is being drained by your screen."
            wastedPercent > 30 -> "Divided Focus. $wastedHours hours were spent on non-essential apps. You are performing at half-capacity."
            else -> "Iron discipline. Only $wastedHours hours lost in 30 days. You are in the top 1% of focused humans."
        }
    }

    private suspend fun getWorkRelatedApps(context: Context): Set<String> {
        return try {
            val db = IronMindDatabase.getDatabase(context)
            db.taskDao().getAllUsedPackageNames().filterNotNull().toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun generateVerdict(wastedPercent: Float, offenders: List<AppUsageData>): String {
        val toxicCount = offenders.count { !it.isAlreadyBlocked }
        val topApp = offenders.firstOrNull()?.appName ?: "distracting apps"
        
        return when {
            toxicCount > 0 -> "Intruder Alert. You spent time on $toxicCount apps that have ZERO work history. Lock them before they grow."
            wastedPercent >= 70 -> "Extreme Dilution. You gave most of yesterday to $topApp. You are working for the apps, not yourself."
            wastedPercent >= 40 -> "Identity Crisis. Nearly half your day was spent on distractions. Fix it today."
            else -> "Elite Focus. Minimal time lost. One more day of this and you'll be unstoppable."
        }
    }

    fun hasUsagePermission(context: Context): Boolean {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            System.currentTimeMillis() - 1000 * 60,
            System.currentTimeMillis()
        )
        return !stats.isNullOrEmpty()
    }

    private fun isSystemApp(context: Context, packageName: String): Boolean {
        return runCatching {
            val info = context.packageManager.getApplicationInfo(packageName, 0)
            (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        }.getOrElse { true }
    }

    fun formatDuration(ms: Long): String {
        val totalMinutes = ms / 1000 / 60
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
}
