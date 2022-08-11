package com.example.map.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient

@SuppressLint("MissingPermission, VisibleForTests")
class LocationClient(context: Context) {
	private val fusedLocationClient = FusedLocationProviderClient(context)

	private val _lastLocation = MutableLiveData<Location>()
	val lastLocation: LiveData<Location> = _lastLocation

	init {
		fusedLocationClient.lastLocation.addOnSuccessListener { location ->
			Log.e("Mapss", "Last location: $location")
			_lastLocation.value = location
		}
	}
}