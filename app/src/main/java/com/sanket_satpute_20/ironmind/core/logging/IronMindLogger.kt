package com.sanket_satpute_20.ironmind.core.logging

import android.util.Log

object IronMindLogger {
    private const val TAG = "IronMindLifecycle"

    fun log(component: String, event: String, params: Map<String, Any?> = emptyMap()) {
        val message = buildString {
            append("[$component] [$event]")
            if (params.isNotEmpty()) {
                append(" ")
                append(params.entries.joinToString(" ") { "${it.key}=${it.value}" })
            }
        }
        Log.d(TAG, message)
    }

    fun e(component: String, event: String, params: Map<String, Any?> = emptyMap(), throwable: Throwable? = null) {
        val message = buildString {
            append("[$component] [$event]")
            if (params.isNotEmpty()) {
                append(" ")
                append(params.entries.joinToString(" ") { "${it.key}=${it.value}" })
            }
        }
        if (throwable != null) {
            Log.e(TAG, message, throwable)
        } else {
            Log.e(TAG, message)
        }
    }
}
