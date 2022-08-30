package com.example.map.data.repo

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.MainThread
import com.example.map.data.database.LocationDao
import com.example.map.data.database.LocationEntity
import com.example.map.di.AppDispatchers
import com.example.map.di.Dispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepo @Inject constructor(
	private val locationDao: LocationDao,
	private val locationManager: BackgroundLocationManager,
	private val externalScope: CoroutineScope,
	@Dispatcher(AppDispatchers.IO) private val defaultDispatcher: CoroutineDispatcher
) {
	/**
	 * Returns all recorded locations from database.
	 */
	fun getLocations(): Flow<List<LocationEntity>> = locationDao.getLocations()

	// Not being used now but could in future versions.
	/**
	 * Returns specific location in database.
	 */
	fun getLocation(id: UUID): Flow<LocationEntity> = locationDao.getLocation(id)

	// Not being used now but could in future versions.
	/**
	 * Updates location in database.
	 */
	fun updateLocation(locationEntity: LocationEntity) {
		externalScope.launch {
			locationDao.updateLocation(locationEntity)
		}
	}

	/**
	 * Adds location to the database.
	 */
	fun addLocation(locationEntity: LocationEntity) {
		externalScope.launch {
			locationDao.addLocation(locationEntity)
		}
	}

	/**
	 * Adds list of locations to the database.
	 */
	fun addLocations(myLocationEntities: List<LocationEntity>) {
		externalScope.launch {
			locationDao.addLocations(myLocationEntities)
		}
	}

	// Location related fields/methods:
	/**
	 * Status of whether the app is actively subscribed to location changes.
	 */
	val receivingLocationUpdates: StateFlow<Boolean> = locationManager.receivingLocationUpdates

	/**
	 * Subscribes to location updates.
	 */
	@MainThread
	fun startLocationUpdates() {
		externalScope.launch {
			locationManager.startLocationUpdates()
		}
	}

	/**
	 * Un-subscribes from location updates.
	 */
	@MainThread
	fun stopLocationUpdates() = locationManager.stopLocationUpdates()

	@MainThread
	fun requestLocationServices(
		activity: Activity,
		resolutionForResult: ActivityResultLauncher<IntentSenderRequest>
	) = locationManager.requestLocationServices(activity, resolutionForResult)

}