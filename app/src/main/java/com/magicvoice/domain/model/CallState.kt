package com.magicvoice.domain.model

sealed class CallState {
    data object Idle : CallState()
    data object Connecting : CallState()
    data class Ringing(val phoneNumber: String) : CallState()
    data class Active(
        val phoneNumber: String,
        val duration: Long,
        val isMuted: Boolean = false,
        val isSpeakerOn: Boolean = false
    ) : CallState()
    data class Ended(val reason: String) : CallState()
    data class Error(val message: String) : CallState()
}
