package com.magicvoice.audio.processor

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.magicvoice.domain.model.VoiceEffect
import com.magicvoice.domain.model.VoiceEffectType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

@Singleton
class VoiceEffectEngine @Inject constructor() {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var audioRecord: AudioRecord? = null

    private val _audioData = MutableStateFlow<ByteArray?>(null)
    val audioData: StateFlow<ByteArray?> = _audioData

    private var isRecording = false
    private var currentEffect: VoiceEffect? = null

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_FACTOR = 2
    }

    private val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * BUFFER_SIZE_FACTOR

    fun startRecording(effect: VoiceEffect) {
        if (isRecording) return
        currentEffect = effect
        try {
            audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize)
            audioRecord?.startRecording()
            isRecording = true
            scope.launch { processAudioStream() }
        } catch (e: Exception) {
            Timber.e(e, "Error starting audio recording")
        }
    }

    private fun processAudioStream() {
        val buffer = ByteArray(bufferSize)
        while (isRecording) {
            val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
            if (read > 0) _audioData.value = applyVoiceEffect(buffer, read)
        }
    }

    private fun applyVoiceEffect(audioData: ByteArray, length: Int): ByteArray {
        val effect = currentEffect ?: return audioData.copyOf(length)
        val shortBuffer = ByteBuffer.wrap(audioData, 0, length).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        val samples = FloatArray(shortBuffer.remaining()) { shortBuffer.get() / 32768f }
        val pitch = applyPitchShift(samples, effect.pitchShift)
        val formant = applyFormantShift(pitch, effect.formantShift)
        val speed = if (effect.speedFactor != 1.0f) applySpeedChange(formant, effect.speedFactor) else formant
        val finalSamples = if (effect.type == VoiceEffectType.ROBOT) applyRobotEffect(speed) else speed
        return samplesToBytes(finalSamples)
    }

    private fun applyPitchShift(samples: FloatArray, semitones: Float): FloatArray {
        if (semitones == 0f) return samples
        val pitchRatio = 2.0.pow(semitones / 12.0).toFloat()
        return resample(samples, pitchRatio)
    }

    private fun applyFormantShift(samples: FloatArray, shift: Float): FloatArray =
        if (shift == 1.0f) samples else samples.map { it * shift }.toFloatArray()

    private fun applySpeedChange(samples: FloatArray, speedFactor: Float): FloatArray =
        if (speedFactor == 1.0f) samples else resample(samples, speedFactor)

    private fun resample(samples: FloatArray, factor: Float): FloatArray {
        val outputLength = (samples.size / factor).toInt().coerceAtLeast(1)
        val output = FloatArray(outputLength)
        for (i in output.indices) {
            val sourceIndex = i * factor
            val idx = sourceIndex.toInt()
            val frac = sourceIndex - idx
            if (idx + 1 < samples.size) output[i] = samples[idx] * (1 - frac) + samples[idx + 1] * frac
        }
        return output
    }

    private fun applyRobotEffect(samples: FloatArray): FloatArray {
        val modulationFreq = 30.0
        return FloatArray(samples.size) { i ->
            val t = i.toDouble() / SAMPLE_RATE
            (samples[i] * sin(2 * PI * modulationFreq * t)).toFloat()
        }
    }

    private fun samplesToBytes(samples: FloatArray): ByteArray {
        val byteBuffer = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        samples.forEach { byteBuffer.putShort((it * 32767f).toInt().coerceIn(-32768, 32767).toShort()) }
        return byteBuffer.array()
    }

    fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    fun updateEffect(effect: VoiceEffect) {
        currentEffect = effect
    }

    fun cleanup() {
        stopRecording()
        scope.cancel()
    }
}
