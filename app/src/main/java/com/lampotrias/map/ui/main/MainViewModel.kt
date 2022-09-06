package com.lampotrias.map.ui.main

import androidx.lifecycle.ViewModel
import com.lampotrias.map.data.PlaceProvider
import com.lampotrias.map.data.repo.LocationRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
	private val locationRepo: LocationRepo,
	private val itemsRepo: PlaceProvider
) : ViewModel() {


	val receivingLocationUpdates: StateFlow<Boolean> = locationRepo.receivingLocationUpdates
	val locationList = locationRepo.getLocations()
	val savedItems = itemsRepo.places

	fun startLocationUpdates() {
		locationRepo.startLocationUpdates()
	}

	fun stopLocationUpdates() {
		locationRepo.stopLocationUpdates()
	}
}