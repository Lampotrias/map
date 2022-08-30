package com.lampotrias.map.di

import com.lampotrias.map.data.database.LocationDao
import com.lampotrias.map.data.database.MapDatabase
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
