package com.magicvoice.ui.call

import androidx.lifecycle.ViewModel
import com.magicvoice.domain.model.CallState
import com.magicvoice.voip.CallManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val callManager: CallManager
) : ViewModel() {
    val callState: StateFlow<CallState> = callManager.callState
    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()
    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()
    private val _callDuration = MutableStateFlow(0L)
    val callDuration: StateFlow<Long> = _callDuration.asStateFlow()

    fun toggleMute() { _isMuted.value = !_isMuted.value; callManager.toggleMute() }
    fun toggleSpeaker() { _isSpeakerOn.value = !_isSpeakerOn.value; callManager.toggleSpeaker() }
    fun endCall() = callManager.endCall()
    fun showEffectSelector() = Unit
}
