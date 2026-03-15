package com.magicvoice.voip

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalingClient @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentCallId: String? = null

    fun sendOffer(sdp: SessionDescription, phoneNumber: String) {
        scope.launch {
            val callData = hashMapOf(
                "offer" to hashMapOf("type" to sdp.type.canonicalForm(), "sdp" to sdp.description),
                "phoneNumber" to phoneNumber,
                "timestamp" to System.currentTimeMillis(),
                "status" to "calling"
            )
            firestore.collection("calls").add(callData).addOnSuccessListener { currentCallId = it.id }
                .addOnFailureListener { Timber.e(it, "Error sending offer") }
        }
    }

    fun sendIceCandidate(candidate: IceCandidate) {
        val callId = currentCallId ?: return
        firestore.collection("calls").document(callId).collection("candidates")
            .add(hashMapOf("sdpMid" to candidate.sdpMid, "sdpMLineIndex" to candidate.sdpMLineIndex, "candidate" to candidate.sdp))
            .addOnFailureListener { Timber.e(it, "Error sending ICE candidate") }
    }

    fun endCall() {
        currentCallId?.let { firestore.collection("calls").document(it).update("status", "ended") }
        currentCallId = null
    }
}
