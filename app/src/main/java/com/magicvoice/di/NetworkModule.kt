package com.magicvoice.di

import android.content.Context
import com.magicvoice.voip.WebRTCManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideWebRTCManager(@ApplicationContext context: Context): WebRTCManager = WebRTCManager(context)
}
