package com.example.map.di

import android.content.Context
import androidx.room.Room
import com.example.map.data.database.MapDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
	@Provides
	@Singleton
	fun providesAppDatabase(
		@ApplicationContext context: Context,
	): MapDatabase = Room.databaseBuilder(
		context,
		MapDatabase::class.java,
		"location_db"
	).build()
}
