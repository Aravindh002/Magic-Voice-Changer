package com.magicvoice.ui.home

import androidx.lifecycle.ViewModel
import com.magicvoice.domain.model.VoiceEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class HomeUiState(
    val userCredits: Int = 100,
    val isPremium: Boolean = false,
    val selectedEffect: VoiceEffect? = VoiceEffect.getDefaultEffects().firstOrNull()
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
