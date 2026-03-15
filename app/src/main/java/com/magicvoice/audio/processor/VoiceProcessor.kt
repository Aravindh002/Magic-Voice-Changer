package com.magicvoice.audio.processor

import com.magicvoice.domain.model.VoiceEffect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceProcessor @Inject constructor() {
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private var currentEffect: VoiceEffect? = null

    fun startProcessing(effect: VoiceEffect) {
        currentEffect = effect
        _isProcessing.value = true
        Timber.d("Voice processing started with effect: ${effect.name}")
    }

    fun updateEffect(effect: VoiceEffect) {
        currentEffect = effect
        Timber.d("Updated voice effect to: ${effect.name}")
    }

    fun stopProcessing() {
        _isProcessing.value = false
        Timber.d("Voice processing stopped")
    }

    fun getProcessedAudio(): FloatArray = FloatArray(2048)
}
