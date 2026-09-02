package com.sanket_satpute_20.ironmind.voice

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class VoiceRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var isRecording = false

    fun startRecording(taskName: String, maxDurationMs: Int = 12_000): File {
        stopRecording() // safety

        val timestamp = System.currentTimeMillis()
        val sanitizedName = taskName.replace("[^a-zA-Z0-9]".toRegex(), "_")
        val fileName = "voice_${sanitizedName}_${timestamp}.3gp"

        val dir = File(context.filesDir, "voice_logs").apply { mkdirs() }
        val file = File(dir, fileName)
        currentFile = file

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(file.absolutePath)
            setMaxDuration(maxDurationMs)
            prepare()
            start()
        }

        isRecording = true
        return file
    }

    fun stopRecording(): File? {
        if (!isRecording) return null
        return runCatching {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            isRecording = false
            currentFile
        }.getOrNull()
    }

    fun isCurrentlyRecording() = isRecording

    fun cancelRecording() {
        runCatching {
            recorder?.apply { stop(); release() }
            recorder = null
            currentFile?.delete()
            currentFile = null
            isRecording = false
        }
    }
}
