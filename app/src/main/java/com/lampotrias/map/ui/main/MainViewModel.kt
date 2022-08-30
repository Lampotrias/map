package com.lampotrias.map.ui.main

import androidx.lifecycle.ViewModel
import com.lampotrias.map.data.repo.LocationRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
	private val repo: LocationRepo
) : ViewModel() {

	val receivingLocationUpdates: StateFlow<Boolean> = repo.receivingLocationUpdates
	val locationList = repo.getLocations()

	fun startLocationUpdates() {
		repo.startLocationUpdates()
	}

	fun stopLocationUpdates() {
		repo.stopLocationUpdates()
	}
}