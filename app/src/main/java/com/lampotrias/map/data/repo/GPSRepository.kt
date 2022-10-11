package com.lampotrias.map.data.repo

import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class GPSRepository @Inject constructor(
	@ApplicationContext
	private val context: Context
) {
	private val gpsMinTime = 500L
	private val gpsMinDistance = 0L

	private val _receivingGpsUpdates: MutableStateFlow<Location> = MutableStateFlow(Location(null))
	val receivingGpsUpdates = _receivingGpsUpdates.asStateFlow()

	private val locationManager =
		context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
	private val locationListener = LocationListener { location ->
		if (location.hasSpeed()) {
			Log.w("adasdas", "speed: ${location.speed}")
			_receivingGpsUpdates.value = location
		}
	}

	@SuppressLint("MissingPermission")
	fun startListening() {
		val criteria = Criteria()
		criteria.accuracy = Criteria.ACCURACY_FINE
		criteria.isSpeedRequired = true
		criteria.isAltitudeRequired = false
		criteria.isBearingRequired = false
		criteria.isCostAllowed = true
		criteria.powerRequirement = Criteria.POWER_LOW
		val bestProvider = locationManager.getBestProvider(criteria, true)

		if (!bestProvider.isNullOrEmpty()) {
			locationManager.requestLocationUpdates(
				bestProvider,
				gpsMinTime,
				gpsMinDistance.toFloat(),
				locationListener
			)
		} else {
			val providers = locationManager.getProviders(true)
			for (provider in providers) {
				locationManager.requestLocationUpdates(
					provider, gpsMinTime,
					gpsMinDistance.toFloat(), locationListener
				)
			}
		}
	}

	fun stopListening() {
		try {
			locationManager.removeUpdates(locationListener)
		} catch (ex: Exception) {
			ex.printStackTrace()
		}
	}
}
