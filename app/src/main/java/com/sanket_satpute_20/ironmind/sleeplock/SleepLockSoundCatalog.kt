package com.sanket_satpute_20.ironmind.sleeplock

data class SleepSoundOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val downloadUrl: String
)

object SleepLockSoundCatalog {
    val sounds = listOf(
        SleepSoundOption(
            id = "rain",
            title = "Rain",
            subtitle = "Soft rainfall for a steady sleep runway.",
            downloadUrl = "https://www.orangefreesounds.com/wp-content/uploads/2016/10/Soothing-rain.mp3"
        ),
        SleepSoundOption(
            id = "brown_noise",
            title = "Brown Noise",
            subtitle = "Deep low-end masking for heavy quiet.",
            downloadUrl = "https://www.orangefreesounds.com/wp-content/uploads/2018/12/Brown-noise-sleep.mp3"
        ),
        SleepSoundOption(
            id = "night_air",
            title = "Night Air",
            subtitle = "Crickets and open-air calm for bedtime.",
            downloadUrl = "https://www.orangefreesounds.com/wp-content/uploads/2017/02/Night-ambience-sound-effect.mp3"
        )
    )

    fun byId(id: String): SleepSoundOption =
        sounds.firstOrNull { it.id == id } ?: sounds.first()

    fun normalizeIds(ids: Set<String>, fallbackId: String = sounds.first().id): Set<String> {
        val validIds = sounds.mapTo(linkedSetOf()) { it.id }
        val filtered = ids.filterTo(linkedSetOf()) { it in validIds }
        return if (filtered.isNotEmpty()) filtered else linkedSetOf(byId(fallbackId).id)
    }
}
