package com.magicvoice.domain.model

enum class VoiceEffectType {
    NORMAL, FEMALE, MALE, KID, OLD_MAN, ROBOT, CARTOON, ALIEN, DEEP_VOICE
}

data class VoiceEffect(
    val id: String,
    val name: String,
    val type: VoiceEffectType,
    val pitchShift: Float,
    val formantShift: Float,
    val speedFactor: Float = 1.0f,
    val iconRes: Int,
    val isPremium: Boolean = false
) {
    companion object {
        fun getDefaultEffects(): List<VoiceEffect> = listOf(
            VoiceEffect("normal", "Normal", VoiceEffectType.NORMAL, 0f, 1.0f, iconRes = android.R.drawable.ic_menu_call),
            VoiceEffect("female", "Female", VoiceEffectType.FEMALE, 4f, 1.2f, iconRes = android.R.drawable.ic_menu_call),
            VoiceEffect("male", "Male", VoiceEffectType.MALE, -4f, 0.8f, iconRes = android.R.drawable.ic_menu_call),
            VoiceEffect("kid", "Kid", VoiceEffectType.KID, 6f, 1.4f, iconRes = android.R.drawable.ic_menu_call),
            VoiceEffect("old_man", "Old Man", VoiceEffectType.OLD_MAN, -3f, 0.7f, 0.9f, android.R.drawable.ic_menu_call),
            VoiceEffect("robot", "Robot", VoiceEffectType.ROBOT, 0f, 0.6f, iconRes = android.R.drawable.ic_menu_call, isPremium = true),
            VoiceEffect("cartoon", "Cartoon", VoiceEffectType.CARTOON, 8f, 1.6f, 1.1f, android.R.drawable.ic_menu_call, true)
        )
    }
}
