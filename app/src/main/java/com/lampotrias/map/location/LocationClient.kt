package com.lampotrias.map.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority

@SuppressLint("MissingPermission, VisibleForTests")
class LocationClient(context: Context) {
	private val fusedLocationClient = FusedLocationProviderClient(context)

	private val _lastLocation = MutableLiveData<Result<Location?>>()
	val lastLocation: LiveData<Result<Location?>> = _lastLocation

	private val _currentLocation = MutableLiveData<Result<Location>>()
	val currentLocation: LiveData<Result<Location>> = _currentLocation

	init {
		fusedLocationClient.lastLocation.addOnSuccessListener { location ->
			Log.e("Mapss", "Last location: $location")
			_lastLocation.value = Result.success(location)
		}.addOnFailureListener { exception ->
			_lastLocation.value = Result.failure(exception)
		}.addOnCanceledListener {
			_lastLocation.value = Result.failure(RuntimeException("canceled"))
		}
	}

	fun getCurrentLocation() {
		fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
			.addOnSuccessListener { location ->
				_currentLocation.value = Result.success(location)
			}.addOnFailureListener { exception ->
				_currentLocation.value = Result.failure(exception)
			}.addOnCanceledListener {
				_currentLocation.value = Result.failure(RuntimeException("canceled"))
			}
	}
}