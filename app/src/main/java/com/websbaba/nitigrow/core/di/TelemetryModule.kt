package com.websbaba.nitigrow.core.di

import com.websbaba.nitigrow.core.telemetry.FirebaseTelemetry
import com.websbaba.nitigrow.core.telemetry.Telemetry
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TelemetryModule {
    @Binds
    @Singleton
    abstract fun bindTelemetry(impl: FirebaseTelemetry): Telemetry
}
