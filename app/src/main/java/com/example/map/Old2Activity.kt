package com.example.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.bonuspack.routing.RoadNode
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayItem


class Old2Activity : AppCompatActivity() {
	private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
	private lateinit var map: MapView

	private lateinit var firstMarker: Marker

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		Configuration.getInstance().userAgentValue = "asdas"
		setContentView(R.layout.activity_main)

		map = findViewById(R.id.map)
		map.setTileSource(TileSourceFactory.MAPNIK)

		val mapController = map.controller
		mapController.setZoom(10.0)
		val startPoint = GeoPoint(54.70920922606956, 20.512250486598862)
		mapController.setCenter(startPoint)

		val items = ArrayList<OverlayItem>()
		items.add(OverlayItem("Title", "Description", GeoPoint(0.0, 0.0)))

		firstMarker = Marker(map)
		firstMarker.position = GeoPoint(55.175226026066454, 21.54602741862484)
		firstMarker.setAnchor(Marker.ANCHOR_RIGHT, Marker.ANCHOR_RIGHT)
		firstMarker.image = ResourcesCompat.getDrawable(
			this@Old2Activity.resources,
			R.drawable.ic_launcher_foreground,
			null
		)
		firstMarker.title = "Title"
		firstMarker.setOnMarkerClickListener { marker, mapView ->
			Toast.makeText(this, "asdada", Toast.LENGTH_SHORT).show()
			marker.showInfoWindow()

			true
		}
		map.overlays.add(firstMarker)

		var overlay = ItemizedOverlayWithFocus(
			items,
			object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
				override fun onItemSingleTapUp(index: Int, item: OverlayItem): Boolean {
					//do something
					return true
				}

				override fun onItemLongPress(index: Int, item: OverlayItem): Boolean {
					return false
				}
			},
			this
		)
		overlay.setFocusItemsOnTap(true)

		map.overlays.add(overlay)


		GlobalScope.launch {
			val roadManager = OSRMRoadManager(this@Old2Activity, "tetst")

			val waypoints = ArrayList<GeoPoint>()
			waypoints.add(startPoint)
			val endPoint = GeoPoint(55.175226026066454, 21.54602741862484)
			waypoints.add(endPoint)

			roadManager.getRoads(waypoints).forEach {
				val roadOverlay = RoadManager.buildRoadOverlay(it)

				for (i in 0 until it.mNodes.size) {
					val node: RoadNode = it.mNodes[i]
					val nodeMarker = Marker(map)
					nodeMarker.position = node.mLocation
					nodeMarker.title = "Step $i"
					map.overlays.add(nodeMarker)
				}

				map.overlays.add(roadOverlay)
			}



			roadManager.setMean(OSRMRoadManager.MEAN_BY_FOOT)

			map.invalidate()
		}

		map.invalidate()

		requestPermissionsIfNecessary(
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.ACCESS_FINE_LOCATION
		)
	}

	override fun onResume() {
		super.onResume()
		//this will refresh the osmdroid configuration on resuming.
		//if you make changes to the configuration, use
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
		map.onResume() //needed for compass, my location overlays, v6.0.0 and up
	}

	override fun onPause() {
		super.onPause()
		//this will refresh the osmdroid configuration on resuming.
		//if you make changes to the configuration, use
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//Configuration.getInstance().save(this, prefs);
		map.onPause()  //needed for compass, my location overlays, v6.0.0 and up
	}

	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String>,
		grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)

		val permissionsToRequest = ArrayList<String>()
		var i = 0
		while (i < grantResults.size) {
			permissionsToRequest.add(permissions[i])
			i++
		}
		if (permissionsToRequest.size > 0) {
			ActivityCompat.requestPermissions(
				this,
				permissionsToRequest.toTypedArray(),
				REQUEST_PERMISSIONS_REQUEST_CODE
			)
		}
	}


	private fun requestPermissionsIfNecessary(vararg permissions: String) {
		val permissionsToRequest = ArrayList<String>()
		permissions.forEach { permission ->
			if (ContextCompat.checkSelfPermission(this, permission)
				!= PackageManager.PERMISSION_GRANTED
			) {
				// Permission is not granted
				permissionsToRequest.add(permission)
			}
		}
		if (permissionsToRequest.size > 0) {
			ActivityCompat.requestPermissions(
				this,
				permissionsToRequest.toTypedArray(),
				REQUEST_PERMISSIONS_REQUEST_CODE
			)
		}
	}
}