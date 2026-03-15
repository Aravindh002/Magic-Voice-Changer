package com.magicvoice.di

import com.magicvoice.audio.processor.VoiceEffectEngine
import com.magicvoice.audio.processor.VoiceProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioModule {
    @Provides @Singleton fun provideVoiceProcessor(): VoiceProcessor = VoiceProcessor()
    @Provides @Singleton fun provideVoiceEffectEngine(): VoiceEffectEngine = VoiceEffectEngine()
}
