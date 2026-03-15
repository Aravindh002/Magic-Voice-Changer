package com.magicvoice.voip

import android.content.Context
import io.getstream.webrtc.android.createPeerConnectionFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RTCStatsCollectorCallback
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRTCManager @Inject constructor(
    private val context: Context
) {
    private var peerConnectionFactory: PeerConnectionFactory = context.createPeerConnectionFactory()
    private var peerConnection: PeerConnection? = null
    private var localAudioTrack: AudioTrack? = null
    private var audioSource: AudioSource? = null

    private val _connectionState = MutableStateFlow(RTCConnectionState.DISCONNECTED)
    val connectionState: StateFlow<RTCConnectionState> = _connectionState

    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer()
    )

    fun createPeerConnection(observer: PeerConnection.Observer) {
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, observer)
        _connectionState.value = RTCConnectionState.CONNECTING
        Timber.d("PeerConnection created")
    }

    fun createAudioTrack(): AudioTrack? {
        val constraints = MediaConstraints()
        audioSource = peerConnectionFactory.createAudioSource(constraints)
        localAudioTrack = peerConnectionFactory.createAudioTrack("local_audio", audioSource)
        return localAudioTrack
    }

    fun addAudioTrack() {
        val track = createAudioTrack() ?: return
        val stream = peerConnectionFactory.createLocalMediaStream("local_stream")
        stream.addTrack(track)
        peerConnection?.addStream(stream)
    }

    fun createOffer(observer: SdpObserver) {
        peerConnection?.createOffer(observer, MediaConstraints())
    }

    fun setLocalDescription(sdp: SessionDescription, observer: SdpObserver) {
        peerConnection?.setLocalDescription(observer, sdp)
    }

    fun setRemoteDescription(sdp: SessionDescription, observer: SdpObserver) {
        peerConnection?.setRemoteDescription(observer, sdp)
    }

    fun addIceCandidate(candidate: IceCandidate) {
        peerConnection?.addIceCandidate(candidate)
    }

    fun closeConnection() {
        localAudioTrack?.dispose()
        audioSource?.dispose()
        peerConnection?.close()
        peerConnection = null
        _connectionState.value = RTCConnectionState.DISCONNECTED
    }

    fun cleanup() {
        closeConnection()
        peerConnectionFactory.dispose()
    }
}

enum class RTCConnectionState { DISCONNECTED, CONNECTING, CONNECTED, FAILED, CLOSED }
