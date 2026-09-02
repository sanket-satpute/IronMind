package com.sanket_satpute_20.ironmind.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.Intent
import android.content.pm.PackageManager

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean
)

object InstalledAppScanner {

    private val knownSafeSystemPackages = setOf(
        "com.android.settings",
        "com.google.android.apps.maps",
        "com.google.android.calendar",
        "com.google.android.apps.translate",
        "com.google.android.apps.files",
        "com.google.android.apps.nbu.files",
        "com.google.android.deskclock",
        "com.google.android.calculator"
    )

    fun scanInstalledApps(context: Context): List<InstalledAppInfo> {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val launchablePackages = pm.queryIntentActivities(launcherIntent, 0)
            .mapTo(hashSetOf()) { it.activityInfo.packageName }

        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .asSequence()
            .filterNot { it.packageName == context.packageName }
            .mapNotNull { app ->
                val isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                    (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                val appName = runCatching { pm.getApplicationLabel(app).toString() }.getOrNull().orEmpty()
                if (appName.isBlank()) return@mapNotNull null
                val isLaunchable = app.packageName in launchablePackages
                if (!isLaunchable && app.packageName !in knownSafeSystemPackages) return@mapNotNull null
                InstalledAppInfo(
                    packageName = app.packageName,
                    appName = appName,
                    isSystemApp = isSystemApp
                )
            }
            .sortedBy { it.appName.lowercase() }
            .toList()
    }

    fun scanSingleInstalledApp(context: Context, packageName: String): InstalledAppInfo? {
        val pm = context.packageManager
        val app = runCatching { pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA) }.getOrNull()
            ?: return null
        val appName = runCatching { pm.getApplicationLabel(app).toString() }.getOrNull().orEmpty()
        if (appName.isBlank()) return null
        val isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
            (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        return InstalledAppInfo(
            packageName = packageName,
            appName = appName,
            isSystemApp = isSystemApp
        )
    }
}
