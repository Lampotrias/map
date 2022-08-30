package com.example.map.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object CoroutinesScopesModule {
	@Singleton
	@Provides
	fun providesCoroutineScope(
		@Dispatcher(AppDispatchers.IO) ioDispatcher: CoroutineDispatcher
	): CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)
}