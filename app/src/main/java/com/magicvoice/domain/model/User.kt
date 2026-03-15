package com.magicvoice.domain.model

data class User(
    val id: String,
    val email: String,
    val name: String,
    val credits: Int = 0,
    val isPremium: Boolean = false,
    val freeMinutesUsed: Int = 0,
    val subscriptionType: SubscriptionType = SubscriptionType.FREE
)

enum class SubscriptionType { FREE, MONTHLY, YEARLY }

data class CallLog(
    val id: String,
    val phoneNumber: String,
    val duration: Long,
    val timestamp: Long,
    val voiceEffect: VoiceEffectType,
    val creditsUsed: Int
)
