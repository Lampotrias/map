package com.example.map.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [LocationEntity::class], version = 1)
@TypeConverters(LocationTypeConverters::class)
abstract class MapDatabase : RoomDatabase() {
	abstract fun locationDao(): LocationDao
}