package com.lampotrias.map.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface LocationDao {
	@Query("SELECT * FROM location ORDER BY recordedAt DESC")
	fun getLocations(): Flow<List<LocationEntity>>

	@Query("SELECT * FROM location WHERE id=(:id)")
	fun getLocation(id: UUID): Flow<LocationEntity>

	@Update
	fun updateLocation(locationEntity: LocationEntity)

	@Insert
	fun addLocation(locationEntity: LocationEntity)

	@Insert
	fun addLocations(locationEntities: List<LocationEntity>)
}