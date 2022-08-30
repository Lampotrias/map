package com.example.map.di

import com.example.map.data.database.LocationDao
import com.example.map.data.database.MapDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DaosModule {
	@Provides
	fun providesAuthorDao(database: MapDatabase): LocationDao = database.locationDao()
}
