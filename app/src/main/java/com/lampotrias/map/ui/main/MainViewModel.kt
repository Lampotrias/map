package com.lampotrias.map.ui.main

import android.location.Location
import androidx.lifecycle.ViewModel
import com.lampotrias.map.data.PlaceProvider
import com.lampotrias.map.data.repo.GPSRepository
import com.lampotrias.map.data.repo.LocationRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
	private val locationRepo: LocationRepo,
	private val gpsRepository: GPSRepository,
	itemsRepo: PlaceProvider
) : ViewModel() {


	val receivingLocationUpdates: StateFlow<Boolean> = locationRepo.receivingLocationUpdates
	private val _receivingGpsUpdates: StateFlow<Location> = gpsRepository.receivingGpsUpdates
	val locationList = locationRepo.getLocations()
	val savedItems = itemsRepo.places

	val speedUpdates = _receivingGpsUpdates.mapNotNull {
		if (it.hasSpeed()) {
			it.speed
		} else null
	}

	fun startLocationUpdates() {
		locationRepo.startLocationUpdates()
	}

	fun stopLocationUpdates() {
		locationRepo.stopLocationUpdates()
	}

	fun startSpeedUpdates() {
		gpsRepository.startListening()
	}

	fun stopSpeedUpdates() {
		gpsRepository.stopListening()
	}
}