package com.example.drumpads

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap

class PadAudioManager(private val context: Context) {

    private val soundPool: SoundPool
    // Maps padIndex (0..3) to SoundPool sampleId
    private val sampleIds = ConcurrentHashMap<Int, Int>()
    private val padsDir: File = File(context.filesDir, "pads")

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(16) // Allows polyphony: rapid retriggers and overlapping pads
            .setAudioAttributes(audioAttributes)
            .build()

        if (!padsDir.exists()) {
            padsDir.mkdirs()
        }

        // Preload any samples stored on disk from previous sessions
        loadSavedSamples()
    }

    private fun getPadFile(padIndex: Int): File = File(padsDir, "pad_$padIndex.audio")

    private fun loadSavedSamples() {
        for (i in 0 until 4) {
            val file = getPadFile(i)
            if (file.exists() && file.length() > 0) {
                loadSampleFile(i, file)
            }
        }
    }

    private fun loadSampleFile(padIndex: Int, file: File) {
        try {
            // Unload old sample if present
            sampleIds[padIndex]?.let { oldSampleId ->
                soundPool.unload(oldSampleId)
            }
            val sampleId = soundPool.load(file.absolutePath, 1)
            sampleIds[padIndex] = sampleId
        } catch (e: Exception) {
            Log.e("PadAudioManager", "Failed loading sample for pad $padIndex", e)
        }
    }

    suspend fun assignSample(padIndex: Int, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val targetFile = getPadFile(padIndex)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return@withContext false

            loadSampleFile(padIndex, targetFile)
            return@withContext true
        } catch (e: Exception) {
            Log.e("PadAudioManager", "Failed to copy and assign sample for pad $padIndex", e)
            return@withContext false
        }
    }

    fun play(padIndex: Int) {
        val sampleId = sampleIds[padIndex] ?: return
        if (sampleId > 0) {
            // Play immediately: leftVolume=1.0, rightVolume=1.0, priority=1, loop=0, rate=1.0
            soundPool.play(sampleId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun hasSample(padIndex: Int): Boolean {
        return sampleIds.containsKey(padIndex) && (sampleIds[padIndex] ?: 0) > 0
    }

    fun release() {
        soundPool.release()
    }
}