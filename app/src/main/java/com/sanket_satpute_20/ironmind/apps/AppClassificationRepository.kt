package com.sanket_satpute_20.ironmind.apps

import android.content.Context
import com.sanket_satpute_20.ironmind.data.AppClassification
import com.sanket_satpute_20.ironmind.data.AppClassificationDao
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import kotlinx.coroutines.flow.Flow

data class ClassifiedApp(
    val packageName: String,
    val appName: String,
    val category: AppCategory,
    val source: AppClassificationSource,
    val confidence: AppClassificationConfidence,
    val lastDecisionSource: String,
    val isSystemApp: Boolean,
    val lastSeenAt: Long,
    val lastPromptedAt: Long,
    val timesPrompted: Int,
    val timesOpenedDuringFocus: Int,
    val timesOpenedOverall: Int,
    val isLockedByUser: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

data class AppRuntimePolicySnapshot(
    val blockedPackages: Set<String>,
    val contextPackages: Set<String>,
    val reviewQueueCount: Int
)

class AppClassificationRepository private constructor(
    private val dao: AppClassificationDao
) {

    fun observeAll(): Flow<List<AppClassification>> = dao.getAll()

    fun observeCategory(category: AppCategory): Flow<List<AppClassification>> {
        return dao.getByCategory(category.wireValue)
    }

    fun observeReviewQueue(): Flow<List<AppClassification>> = dao.getReviewQueue()

    suspend fun syncInstalledApps(context: Context) {
        val installedApps = InstalledAppScanner.scanInstalledApps(context)
        val existing = dao.getAllOnce().associateBy { it.packageName }
        val now = System.currentTimeMillis()

        val merged = installedApps.map { installed ->
            val current = existing[installed.packageName]
            if (current != null) {
                current.copy(
                    appName = installed.appName,
                    isSystemApp = installed.isSystemApp,
                    lastSeenAt = now,
                    updatedAt = now
                )
            } else {
                val seed = DefaultAppClassifier.getSeed(installed.packageName)
                AppClassification(
                    packageName = installed.packageName,
                    appName = installed.appName.ifBlank { seed?.appName ?: installed.packageName },
                    category = inferDefaultCategory(installed, seed).wireValue,
                    source = inferDefaultSource(seed).wireValue,
                    confidence = inferDefaultConfidence(installed, seed).wireValue,
                    lastDecisionSource = "SYSTEM_SYNC",
                    isSystemApp = installed.isSystemApp,
                    lastSeenAt = now,
                    lastPromptedAt = 0L,
                    timesPrompted = 0,
                    timesOpenedDuringFocus = 0,
                    timesOpenedOverall = 0,
                    isLockedByUser = false,
                    createdAt = now,
                    updatedAt = now
                )
            }
        }

        if (merged.isNotEmpty()) {
            dao.upsertAll(merged)
        }
    }

    suspend fun importLegacyBlockedApps(blockedPackages: Set<String>) {
        if (blockedPackages.isEmpty()) return
        val now = System.currentTimeMillis()
        val updates = blockedPackages.mapNotNull { packageName ->
            val current = dao.getByPackage(packageName) ?: return@mapNotNull null
            if (current.isLockedByUser) return@mapNotNull null
            current.copy(
                category = AppCategory.VOID.wireValue,
                source = AppClassificationSource.USER_CONFIRMED.wireValue,
                lastDecisionSource = "LEGACY_BLOCKLIST_IMPORT",
                updatedAt = now
            )
        }
        if (updates.isNotEmpty()) {
            dao.upsertAll(updates)
        }
    }

    suspend fun getClassification(packageName: String): ClassifiedApp? {
        return dao.getByPackage(packageName)?.toDomain()
    }

    suspend fun getContextApps(): List<ClassifiedApp> {
        return dao.getContextAppsOnce()
            .map { it.toDomain() }
            .filter { it.source != AppClassificationSource.UNKNOWN_PENDING }
            .sortedBy { it.appName.lowercase() }
    }

    suspend fun getReviewQueue(): List<ClassifiedApp> {
        return dao.getAllOnce()
            .asSequence()
            .filter { AppClassificationSource.fromWireValue(it.source) == AppClassificationSource.UNKNOWN_PENDING }
            .sortedWith(compareByDescending<AppClassification> { it.timesOpenedDuringFocus }.thenBy { it.appName.lowercase() })
            .map { it.toDomain() }
            .toList()
    }

    suspend fun getRuntimePolicySnapshot(): AppRuntimePolicySnapshot {
        val all = dao.getAllOnce()
        val blocked = all.asSequence()
            .filter {
                when (AppCategory.fromWireValue(it.category)) {
                    AppCategory.VOID, AppCategory.SIGNAL -> true
                    AppCategory.TOOL, AppCategory.CONTEXT -> false
                }
            }
            .mapTo(linkedSetOf()) { it.packageName }
        val context = all.asSequence()
            .filter { AppCategory.fromWireValue(it.category) == AppCategory.CONTEXT }
            .mapTo(linkedSetOf()) { it.packageName }
        val pendingCount = all.count { AppClassificationSource.fromWireValue(it.source) == AppClassificationSource.UNKNOWN_PENDING }
        return AppRuntimePolicySnapshot(
            blockedPackages = blocked,
            contextPackages = context,
            reviewQueueCount = pendingCount
        )
    }

    suspend fun getBlockedLikePackagesSnapshot(): Set<String> {
        return getRuntimePolicySnapshot().blockedPackages
    }

    suspend fun overrideCategory(
        packageName: String,
        category: AppCategory,
        decisionSource: String = "USER_OVERRIDE"
    ) {
        val current = dao.getByPackage(packageName) ?: return
        val now = System.currentTimeMillis()
        dao.upsert(
            current.copy(
                category = category.wireValue,
                source = AppClassificationSource.USER_OVERRIDE.wireValue,
                lastDecisionSource = decisionSource,
                isLockedByUser = true,
                updatedAt = now
            )
        )
    }

    suspend fun confirmCategory(
        packageName: String,
        category: AppCategory,
        decisionSource: String = "USER_CONFIRMED"
    ) {
        val current = dao.getByPackage(packageName) ?: return
        val now = System.currentTimeMillis()
        dao.upsert(
            current.copy(
                category = category.wireValue,
                source = AppClassificationSource.USER_CONFIRMED.wireValue,
                lastDecisionSource = decisionSource,
                updatedAt = now
            )
        )
    }

    suspend fun overrideCategories(
        packageNames: Collection<String>,
        category: AppCategory,
        decisionSource: String
    ) {
        if (packageNames.isEmpty()) return
        packageNames.forEach { packageName ->
            overrideCategory(packageName, category, decisionSource)
        }
    }

    suspend fun setQuickProtectMode(
        packageName: String,
        mode: QuickProtectMode,
        decisionSource: String
    ) {
        val current = dao.getByPackage(packageName) ?: return
        val currentCategory = AppCategory.fromWireValue(current.category)
        val nextCategory = when (mode) {
            QuickProtectMode.BLOCKED -> {
                if (currentCategory == AppCategory.SIGNAL) AppCategory.SIGNAL else AppCategory.VOID
            }
            QuickProtectMode.ASK -> AppCategory.CONTEXT
            QuickProtectMode.ALLOWED -> AppCategory.TOOL
        }
        overrideCategory(packageName, nextCategory, decisionSource)
    }

    suspend fun recordAppSeen(context: Context, packageName: String, duringFocus: Boolean) {
        val now = System.currentTimeMillis()
        val existing = dao.getByPackage(packageName)
        if (existing == null) {
            val seed = DefaultAppClassifier.getSeed(packageName)
            val installedApp = InstalledAppScanner.scanSingleInstalledApp(context, packageName)
            dao.upsert(
                AppClassification(
                    packageName = packageName,
                    appName = installedApp?.appName ?: seed?.appName ?: packageName,
                    category = (seed?.category ?: if (installedApp?.isSystemApp == true) AppCategory.TOOL else AppCategory.CONTEXT).wireValue,
                    source = inferDefaultSource(seed).wireValue,
                    confidence = (seed?.confidence ?: if (installedApp?.isSystemApp == true) AppClassificationConfidence.HIGH else AppClassificationConfidence.LOW).wireValue,
                    lastDecisionSource = if (seed != null) "SEED_DISCOVERY" else "RUNTIME_DISCOVERY",
                    isSystemApp = installedApp?.isSystemApp == true,
                    lastSeenAt = now,
                    lastPromptedAt = 0L,
                    timesPrompted = 0,
                    timesOpenedDuringFocus = if (duringFocus) 1 else 0,
                    timesOpenedOverall = 1,
                    isLockedByUser = false,
                    createdAt = now,
                    updatedAt = now
                )
            )
            return
        }

        if (duringFocus) {
            dao.recordFocusOpen(packageName, now)
        } else {
            dao.recordSeen(packageName, now)
        }
    }

    suspend fun markPrompted(packageName: String) {
        dao.markPrompted(packageName, System.currentTimeMillis())
    }

    suspend fun getLegacyBlockedPackagesSnapshot(): Set<String> {
        val all = dao.getAllOnce()
        return all.filter { classification ->
            val category = AppCategory.fromWireValue(classification.category)
            category == AppCategory.VOID || category == AppCategory.SIGNAL
        }.mapTo(linkedSetOf()) { it.packageName }
    }

    private fun inferDefaultCategory(
        installed: InstalledAppInfo,
        seed: AppClassificationSeed?
    ): AppCategory {
        return when {
            seed != null -> seed.category
            installed.isSystemApp -> AppCategory.TOOL
            else -> AppCategory.CONTEXT
        }
    }

    private fun inferDefaultSource(seed: AppClassificationSeed?): AppClassificationSource {
        return if (seed != null) {
            AppClassificationSource.SYSTEM_DEFAULT
        } else {
            AppClassificationSource.UNKNOWN_PENDING
        }
    }

    private fun inferDefaultConfidence(
        installed: InstalledAppInfo,
        seed: AppClassificationSeed?
    ): AppClassificationConfidence {
        return when {
            seed != null -> seed.confidence
            installed.isSystemApp -> AppClassificationConfidence.HIGH
            else -> AppClassificationConfidence.LOW
        }
    }

    companion object {
        @Volatile private var INSTANCE: AppClassificationRepository? = null

        fun getInstance(context: Context): AppClassificationRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppClassificationRepository(
                    IronMindDatabase.getDatabase(context.applicationContext).appClassificationDao()
                ).also { INSTANCE = it }
            }
        }
    }
}

private fun AppClassification.toDomain(): ClassifiedApp {
    return ClassifiedApp(
        packageName = packageName,
        appName = appName,
        category = AppCategory.fromWireValue(category),
        source = AppClassificationSource.fromWireValue(source),
        confidence = AppClassificationConfidence.fromWireValue(confidence),
        lastDecisionSource = lastDecisionSource,
        isSystemApp = isSystemApp,
        lastSeenAt = lastSeenAt,
        lastPromptedAt = lastPromptedAt,
        timesPrompted = timesPrompted,
        timesOpenedDuringFocus = timesOpenedDuringFocus,
        timesOpenedOverall = timesOpenedOverall,
        isLockedByUser = isLockedByUser,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
