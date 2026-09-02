package com.sanket_satpute_20.ironmind.sleeplock

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.ironmind.apps.InstalledAppInfo
import com.sanket_satpute_20.ironmind.apps.InstalledAppScanner
import com.sanket_satpute_20.ironmind.data.HistoryRecorder
import com.sanket_satpute_20.ironmind.data.PrefManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime

data class SleepSoundUiState(
    val id: String,
    val title: String,
    val subtitle: String,
    val downloaded: Boolean = false,
    val downloading: Boolean = false,
    val error: String? = null
)

data class SleepLockUiState(
    val enabled: Boolean = false,
    val onboardingComplete: Boolean = false,
    val bedtime: LocalTime = LocalTime.of(22, 30),
    val wakeTime: LocalTime = LocalTime.of(6, 30),
    val wakeTimeLockedByChallenge: Boolean = false,
    val warning30Enabled: Boolean = true,
    val warning15Enabled: Boolean = true,
    val warningFinalEnabled: Boolean = true,
    val silenceNotifications: Boolean = true,
    val soundEnabled: Boolean = true,
    val soundMode: SleepLockSoundMode = SleepLockSoundMode.SINGLE,
    val selectedSound: String = "rain",
    val selectedSounds: Set<String> = setOf("rain"),
    val emergencyApps: Set<String> = emptySet(),
    val coreEmergencyPackages: Set<String> = emptySet(),
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val dndPermissionGranted: Boolean = false,
    val notificationsPermissionGranted: Boolean = false,
    val stage: SleepLockStage = SleepLockStage.OFF,
    val scheduleLabel: String = "",
    val nextTransitionLabel: String = "",
    val soundOptions: List<SleepSoundUiState> = emptyList(),
    val emergencyExitRemaining: Int = 0,
    val emergencyOverrideActive: Boolean = false
)

class SleepLockViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val prefs = PrefManager.getInstance(application)
    private val manager = SleepLockManager(application)
    private val soundController = SleepLockSoundController(application)

    var uiState by mutableStateOf(buildState())
        private set

    init {
        refreshInstalledApps()
    }

    fun refreshPermissionState() {
        uiState = buildState(installedApps = uiState.installedApps, previousSoundOptions = uiState.soundOptions)
    }

    fun setEnabled(value: Boolean) {
        uiState = uiState.copy(enabled = value)
    }

    fun setBedtime(value: LocalTime) {
        uiState = uiState.copy(bedtime = value)
    }

    fun setWakeTime(value: LocalTime) {
        if (prefs.challengeActive) return
        uiState = uiState.copy(wakeTime = value)
    }

    fun setWarning30Enabled(value: Boolean) {
        uiState = uiState.copy(warning30Enabled = value)
    }

    fun setWarning15Enabled(value: Boolean) {
        uiState = uiState.copy(warning15Enabled = value)
    }

    fun setWarningFinalEnabled(value: Boolean) {
        uiState = uiState.copy(warningFinalEnabled = value)
    }

    fun setSilenceNotifications(value: Boolean) {
        uiState = uiState.copy(silenceNotifications = value)
    }

    fun setSoundEnabled(value: Boolean) {
        uiState = uiState.copy(soundEnabled = value)
    }

    fun setSoundMode(value: SleepLockSoundMode) {
        uiState = uiState.copy(soundMode = value)
    }

    fun setSelectedSound(value: String) {
        val normalized = SleepLockSoundCatalog.normalizeIds(uiState.selectedSounds + value, value)
        uiState = uiState.copy(selectedSound = value, selectedSounds = normalized)
    }

    fun toggleSelectedSound(value: String) {
        val next = uiState.selectedSounds.toMutableSet()
        if (value in next && next.size > 1) {
            next.remove(value)
        } else {
            next.add(value)
        }
        val normalized = SleepLockSoundCatalog.normalizeIds(next, uiState.selectedSound)
        val nextCurrent = if (uiState.selectedSound in normalized) {
            uiState.selectedSound
        } else {
            normalized.first()
        }
        uiState = uiState.copy(
            selectedSounds = normalized,
            selectedSound = nextCurrent
        )
    }

    fun runTestAction(action: String) {
        save()
        SleepLockAlarmReceiver.dispatchNow(getApplication(), action)
        uiState = buildState(installedApps = uiState.installedApps, previousSoundOptions = uiState.soundOptions)
    }

    fun downloadSound(soundId: String) {
        uiState = uiState.copy(
            soundOptions = uiState.soundOptions.map {
                if (it.id == soundId) it.copy(downloading = true, error = null) else it
            }
        )
        viewModelScope.launch {
            val result = soundController.downloadSound(soundId)
            uiState = uiState.copy(
                soundOptions = uiState.soundOptions.map {
                    if (it.id == soundId) {
                        it.copy(
                            downloaded = result.isSuccess && soundController.isDownloaded(soundId),
                            downloading = false,
                            error = result.exceptionOrNull()?.message
                        )
                    } else {
                        it
                    }
                }
            )
            uiState = buildState(installedApps = uiState.installedApps, previousSoundOptions = uiState.soundOptions)
        }
    }

    fun toggleEmergencyApp(packageName: String) {
        val next = uiState.emergencyApps.toMutableSet()
        if (!next.add(packageName)) {
            next.remove(packageName)
        }
        uiState = uiState.copy(emergencyApps = next)
    }

    fun save() {
        val previousEnabled = prefs.sleepLockEnabled
        val previousBedtime = LocalTime.of(prefs.sleepLockBedHour, prefs.sleepLockBedMinute)
        val previousWake = LocalTime.of(prefs.sleepLockWakeHour, prefs.sleepLockWakeMinute)
        val previousEmergencyApps = prefs.sleepLockEmergencyApps
        val previousSilence = prefs.sleepLockSilenceNotifications
        val previousSoundEnabled = prefs.sleepLockSoundEnabled
        val previousSoundMode = prefs.sleepLockSoundMode
        val previousSound = prefs.sleepLockSelectedSound
        val previousSounds = prefs.sleepLockSelectedSounds

        prefs.sleepLockEnabled = uiState.enabled
        prefs.sleepLockBedHour = uiState.bedtime.hour
        prefs.sleepLockBedMinute = uiState.bedtime.minute
        val enforcedWakeTime = if (prefs.challengeActive) LocalTime.of(5, 0) else uiState.wakeTime
        prefs.sleepLockWakeHour = enforcedWakeTime.hour
        prefs.sleepLockWakeMinute = enforcedWakeTime.minute
        prefs.sleepLockWarning30Enabled = uiState.warning30Enabled
        prefs.sleepLockWarning15Enabled = uiState.warning15Enabled
        prefs.sleepLockWarningFinalEnabled = uiState.warningFinalEnabled
        prefs.sleepLockSilenceNotifications = uiState.silenceNotifications && manager.isDndPermissionGranted()
        prefs.sleepLockSoundEnabled = uiState.soundEnabled
        prefs.sleepLockSoundMode = uiState.soundMode.name
        prefs.sleepLockSelectedSound = uiState.selectedSound
        prefs.sleepLockSelectedSounds = SleepLockSoundCatalog.normalizeIds(uiState.selectedSounds, uiState.selectedSound)
        prefs.sleepLockEmergencyApps = uiState.emergencyApps
        prefs.sleepLockOnboardingComplete = true

        val context = getApplication<Application>()
        if (previousEnabled != prefs.sleepLockEnabled) {
            HistoryRecorder.recordConfigChange(context, "SLEEP_LOCK_ENABLED", previousEnabled, prefs.sleepLockEnabled, "SLEEP_LOCK_SETUP")
        }
        if (previousBedtime != uiState.bedtime) {
            HistoryRecorder.recordConfigChange(context, "SLEEP_LOCK_BEDTIME", previousBedtime.toString(), uiState.bedtime.toString(), "SLEEP_LOCK_SETUP")
        }
        if (previousWake != enforcedWakeTime) {
            HistoryRecorder.recordConfigChange(context, "SLEEP_LOCK_WAKE_TIME", previousWake.toString(), enforcedWakeTime.toString(), "SLEEP_LOCK_SETUP")
        }
        if (previousEmergencyApps != uiState.emergencyApps) {
            HistoryRecorder.recordConfigChange(context, "SLEEP_LOCK_EMERGENCY_APPS", previousEmergencyApps, uiState.emergencyApps, "SLEEP_LOCK_SETUP")
        }
        if (previousSilence != prefs.sleepLockSilenceNotifications) {
            HistoryRecorder.recordConfigChange(context, "SLEEP_LOCK_SILENCE_NOTIFICATIONS", previousSilence, prefs.sleepLockSilenceNotifications, "SLEEP_LOCK_SETUP")
        }
        if (previousSoundEnabled != prefs.sleepLockSoundEnabled) {
            HistoryRecorder.recordConfigChange(context, "SLEEP_LOCK_SOUND_ENABLED", previousSoundEnabled, prefs.sleepLockSoundEnabled, "SLEEP_LOCK_SETUP")
        }
        if (previousSoundMode != prefs.sleepLockSoundMode) {
            HistoryRecorder.recordConfigChange(context, "SLEEP_LOCK_SOUND_MODE", previousSoundMode, prefs.sleepLockSoundMode, "SLEEP_LOCK_SETUP")
        }
        if (previousSound != prefs.sleepLockSelectedSound) {
            HistoryRecorder.recordConfigChange(context, "SLEEP_LOCK_SELECTED_SOUND", previousSound, prefs.sleepLockSelectedSound, "SLEEP_LOCK_SETUP")
        }
        if (previousSounds != prefs.sleepLockSelectedSounds) {
            HistoryRecorder.recordConfigChange(context, "SLEEP_LOCK_SELECTED_SOUNDS", previousSounds, prefs.sleepLockSelectedSounds, "SLEEP_LOCK_SETUP")
        }

        if (prefs.sleepLockEnabled) {
            SleepLockAlarmScheduler.schedule(context)
            val activeNow = manager.syncPersistentState()
            if (activeNow) {
                if (prefs.sleepLockSilenceNotifications) {
                    SleepLockPolicyManager(context).activateNightSilence()
                } else {
                    SleepLockPolicyManager(context).restoreNotificationPolicy()
                }

                if (prefs.sleepLockSoundEnabled) {
                    soundController.play()
                } else {
                    soundController.stop()
                }

                if (!previousEnabled) {
                    SleepLockAlarmReceiver.dispatchNow(context, SleepLockAlarmReceiver.ACTION_START)
                }
            }
        } else {
            SleepLockAlarmScheduler.cancel(context)
            soundController.stop()
            SleepLockPolicyManager(context).restoreNotificationPolicy()
            prefs.sleepLockActive = false
            prefs.sleepLockStartedAt = 0L
            prefs.sleepLockEndsAt = 0L
            prefs.sleepLockLastWarningStage = ""
        }

        uiState = buildState(installedApps = uiState.installedApps, previousSoundOptions = uiState.soundOptions)
    }

    private fun refreshInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val installedApps = InstalledAppScanner.scanInstalledApps(getApplication())
                .filterNot { it.packageName in manager.coreEmergencyPackages() }
            viewModelScope.launch {
                uiState = buildState(installedApps = installedApps, previousSoundOptions = uiState.soundOptions)
            }
        }
    }

    private fun buildState(
        installedApps: List<InstalledAppInfo> = emptyList(),
        previousSoundOptions: List<SleepSoundUiState> = emptyList()
    ): SleepLockUiState {
        manager.syncPersistentState()
        return SleepLockUiState(
            selectedSounds = SleepLockSoundCatalog.normalizeIds(
                prefs.sleepLockSelectedSounds,
                prefs.sleepLockSelectedSound
            ),
            enabled = prefs.sleepLockEnabled,
            onboardingComplete = prefs.sleepLockOnboardingComplete,
            bedtime = LocalTime.of(prefs.sleepLockBedHour, prefs.sleepLockBedMinute),
            wakeTime = if (prefs.challengeActive) LocalTime.of(5, 0) else LocalTime.of(prefs.sleepLockWakeHour, prefs.sleepLockWakeMinute),
            wakeTimeLockedByChallenge = prefs.challengeActive,
            warning30Enabled = prefs.sleepLockWarning30Enabled,
            warning15Enabled = prefs.sleepLockWarning15Enabled,
            warningFinalEnabled = prefs.sleepLockWarningFinalEnabled,
            silenceNotifications = prefs.sleepLockSilenceNotifications,
            soundEnabled = prefs.sleepLockSoundEnabled,
            soundMode = SleepLockSoundMode.fromWireValue(prefs.sleepLockSoundMode),
            selectedSound = SleepLockSoundCatalog.byId(prefs.sleepLockSelectedSound).id,
            emergencyApps = prefs.sleepLockEmergencyApps,
            coreEmergencyPackages = manager.coreEmergencyPackages(),
            installedApps = installedApps,
            dndPermissionGranted = manager.isDndPermissionGranted(),
            notificationsPermissionGranted = manager.areNotificationsEnabled(),
            stage = manager.getStage(),
            scheduleLabel = manager.scheduleLabel(),
            nextTransitionLabel = manager.nextTransitionLabel(),
            emergencyExitRemaining = manager.remainingEmergencyExitAllowance(),
            emergencyOverrideActive = manager.isEmergencyOverrideActive(),
            soundOptions = SleepLockSoundCatalog.sounds.map { sound ->
                SleepSoundUiState(
                    id = sound.id,
                    title = sound.title,
                    subtitle = sound.subtitle,
                    downloaded = soundController.isDownloaded(sound.id),
                    downloading = previousSoundOptions.firstOrNull { it.id == sound.id }?.downloading == true,
                    error = previousSoundOptions.firstOrNull { it.id == sound.id }?.error
                )
            }
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.AndroidViewModelFactory() {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                return SleepLockViewModel(application) as T
            }
        }
    }
}
