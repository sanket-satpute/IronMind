package com.sanket_satpute_20.ironmind.failure

import android.content.Context
import com.sanket_satpute_20.ironmind.data.PrefManager

class PrefManagerFailureStorage(
    context: Context
) : FailureStorage {

    private val prefs =
        PrefManager.getInstance(context.applicationContext)

    override var latestJson: String
        get() = prefs.failureLatestJson
        set(value) {
            prefs.failureLatestJson = value
        }

    override var historyJson: String
        get() = prefs.failureHistoryJson
        set(value) {
            prefs.failureHistoryJson = value
        }

    override var recoveryJson: String
        get() = prefs.recoveryCurrentJson
        set(value) {
            prefs.recoveryCurrentJson = value
        }
}
