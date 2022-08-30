package com.example.map.data.repo

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.map.data.database.LocationEntity
import com.google.android.gms.location.LocationResult
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class LocationUpdatesBroadcastReceiver : BroadcastReceiver() {

	@Inject
	lateinit var repo: LocationRepo

	override fun onReceive(context: Context, intent: Intent) {
		Log.d(TAG, "onReceive() context:$context, intent:$intent")

		if (intent.action == ACTION_PROCESS_UPDATES) {

			// LocationAvailability.isLocationAvailable returns true if the device location is
			// known and reasonably up to date within the hints requested by the active LocationRequests.
			// Failure to determine location may result from a number of causes including disabled
			// location settings or an inability to retrieve sensor data in the device's environment.

//			LocationAvailability.extractLocationAvailability(intent)?.let {
//				if (!PermissionUtils.isLocationPermissionGranted(context)) {
//					Log.w(TAG, "Background location permissions have been revoked!")
			// Note: Clearing existing notifications to prevent duplication. In production
			// you should never just dismiss all notifications.
//					context.cancelAllNotifications()
//					context.showLocationUnavailableNotification(
//						context.getString(R.string.location_background_permission_revoked),
//						context.getString(R.string.location_rationale)
//					)
//				}
//
//				if (!it.isLocationAvailable) {
//					Log.w(TAG, "Location is currently unavailable!")
//				}
//
//				if (!context.isLocationEnabled()) {
//					Log.w(TAG, "Location Services were disabled by the user!")
//					with(liveSharedPreferences.preferences.edit()) {
//						putBoolean("location_services_enabled", false)
//						apply()
//					}
//					 Note: Clearing existing notifications to prevent duplication. In production
//					 you should never just dismiss all notifications.
//					context.cancelAllNotifications()
//					context.showLocationUnavailableNotification(
//						context.getString(R.string.location_service_disabled),
//						context.getString(R.string.enable_location_services_text)
//					)
//				}
//			}

			LocationResult.extractResult(intent)?.let { locationResult ->
				val locations = locationResult.locations.map {
					LocationEntity(
						latitude = it.latitude,
						longitude = it.longitude,
						foreground = isAppInForeground(context),
						recordedAt = Date(it.time)
					)
				}
				if (locations.isNotEmpty()) {
					repo.addLocations(locations)
				}
			}
		}
	}

	// Note: This function's implementation is only for debugging purposes. If you are going to do
	// this in a production app, you should instead track the state of all your activities in a
	// process via android.app.Application.ActivityLifecycleCallbacks's
	// unregisterActivityLifecycleCallbacks(). For more information, check out the link:
	// https://developer.android.com/reference/android/app/Application.html#unregisterActivityLifecycleCallbacks(android.app.Application.ActivityLifecycleCallbacks
	private fun isAppInForeground(context: Context): Boolean {
		val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
		val appProcesses = activityManager.runningAppProcesses ?: return false

		appProcesses.forEach { appProcess ->
			if (appProcess.importance ==
				ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
				appProcess.processName == context.packageName
			) {
				return true
			}
		}
		return false
	}

	companion object {
		private val TAG = LocationUpdatesBroadcastReceiver::class.simpleName

		const val ACTION_PROCESS_UPDATES = "ai.a2i2.locationtracking.action.PROCESS_UPDATES"
	}
}