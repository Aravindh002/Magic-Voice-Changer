package com.magicvoice.voip

import com.magicvoice.audio.processor.VoiceEffectEngine
import com.magicvoice.domain.model.CallState
import com.magicvoice.domain.model.VoiceEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallManager @Inject constructor(
    private val webRTCManager: WebRTCManager,
    private val voiceEffectEngine: VoiceEffectEngine,
    private val signalingClient: SignalingClient
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState

    private var callStartTime: Long = 0
    private var isMuted = false
    private var isSpeakerOn = false
    private var currentPhoneNumber: String? = null

    private val peerConnectionObserver = object : PeerConnection.Observer {
        override fun onIceCandidate(candidate: IceCandidate?) { candidate?.let(signalingClient::sendIceCandidate) }
        override fun onDataChannel(dataChannel: DataChannel?) = Unit
        override fun onIceConnectionReceivingChange(p0: Boolean) = Unit
        override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
            when (state) {
                PeerConnection.IceConnectionState.CONNECTED -> _callState.value = CallState.Active(currentPhoneNumber.orEmpty(), 0, isMuted, isSpeakerOn)
                PeerConnection.IceConnectionState.DISCONNECTED, PeerConnection.IceConnectionState.FAILED -> endCall("Connection lost")
                else -> Unit
            }
        }
        override fun onConnectionChange(state: PeerConnection.PeerConnectionState?) = Unit
        override fun onAddStream(stream: MediaStream?) = Unit
        override fun onRemoveStream(stream: MediaStream?) = Unit
        override fun onSignalingChange(state: PeerConnection.SignalingState?) = Unit
        override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) = Unit
        override fun onRenegotiationNeeded() = Unit
        override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) = Unit
        override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) = Unit
    }

    fun startCall(phoneNumber: String, voiceEffect: VoiceEffect) {
        scope.launch {
            currentPhoneNumber = phoneNumber
            _callState.value = CallState.Connecting
            webRTCManager.createPeerConnection(peerConnectionObserver)
            webRTCManager.addAudioTrack()
            voiceEffectEngine.startRecording(voiceEffect)
            webRTCManager.createOffer(object : SdpObserver {
                override fun onCreateSuccess(sdp: SessionDescription?) {
                    sdp ?: return
                    webRTCManager.setLocalDescription(sdp, object : SdpObserver {
                        override fun onSetSuccess() { signalingClient.sendOffer(sdp, phoneNumber); _callState.value = CallState.Ringing(phoneNumber) }
                        override fun onSetFailure(error: String?) { _callState.value = CallState.Error(error ?: "Failed local description") }
                        override fun onCreateSuccess(p0: SessionDescription?) = Unit
                        override fun onCreateFailure(p0: String?) = Unit
                    })
                }
                override fun onSetSuccess() = Unit
                override fun onCreateFailure(error: String?) { _callState.value = CallState.Error(error ?: "Failed to create offer") }
                override fun onSetFailure(error: String?) = Unit
            })
            callStartTime = System.currentTimeMillis()
        }
    }

    fun endCall(reason: String = "User ended call") {
        voiceEffectEngine.stopRecording()
        webRTCManager.closeConnection()
        signalingClient.endCall()
        _callState.value = CallState.Ended(reason)
        currentPhoneNumber = null
        callStartTime = 0
    }

    fun toggleMute() { isMuted = !isMuted; updateCallState() }
    fun toggleSpeaker() { isSpeakerOn = !isSpeakerOn; updateCallState() }
    fun changeVoiceEffect(effect: VoiceEffect) { voiceEffectEngine.updateEffect(effect); Timber.d("Voice effect changed ${effect.name}") }

    private fun updateCallState() {
        val current = _callState.value
        if (current is CallState.Active) {
            _callState.value = current.copy(isMuted = isMuted, isSpeakerOn = isSpeakerOn, duration = System.currentTimeMillis() - callStartTime)
        }
    }

    fun handleAnswer(sdp: SessionDescription) {
        webRTCManager.setRemoteDescription(sdp, object : SdpObserver {
            override fun onSetSuccess() = Unit
            override fun onSetFailure(error: String?) { _callState.value = CallState.Error(error ?: "Failed remote description") }
            override fun onCreateSuccess(p0: SessionDescription?) = Unit
            override fun onCreateFailure(p0: String?) = Unit
        })
    }

    fun handleIceCandidate(candidate: IceCandidate) = webRTCManager.addIceCandidate(candidate)
}
