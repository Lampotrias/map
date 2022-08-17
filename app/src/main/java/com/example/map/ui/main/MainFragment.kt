package com.example.map.ui.main

import android.Manifest
import android.app.AlertDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.map.R
import com.example.map.data.PlaceProvider
import com.example.map.databinding.FragmentMainBinding
import com.example.map.location.LocationClient
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.bonuspack.utils.BonusPackHelper
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay

@OptIn(DelicateCoroutinesApi::class)
class MainFragment : Fragment() {

	private lateinit var viewModel: MainViewModel
	private var _binding: FragmentMainBinding? = null
	private val binding get() = _binding!!

	private lateinit var locationClient: LocationClient
	private lateinit var currentPositionMarker: Marker
	private var roadOverlay: Overlay? = null

	private val requestPermissionLauncher =
		registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultMap ->
			val deniedPerms = resultMap.filter { entry -> !entry.value }
			if (deniedPerms.isEmpty()) {
				initMap()
			} else {
				val builder = activity?.let {
					AlertDialog.Builder(it)
				}?.also {
					it.setTitle("Missing permissions")
					it.setMessage(deniedPerms.keys.toString())
				}
				builder?.show()
			}
		}

	private fun initMap() {
		Configuration.getInstance().userAgentValue = "asdas"
		with(binding.map) {
			setTileSource(TileSourceFactory.MAPNIK)
			setMultiTouchControls(true)
			controller.setZoom(18.0)
		}

		val mRotationGestureOverlay = RotationGestureOverlay(binding.map)
		mRotationGestureOverlay.isEnabled = true
		binding.map.overlays.add(mRotationGestureOverlay)

		val mCompassOverlay =
			CompassOverlay(context, InternalCompassOrientationProvider(context), binding.map)
		mCompassOverlay.enableCompass()
		binding.map.overlays.add(mCompassOverlay)


		binding.map.invalidate()

		locationClient.lastLocation.observe(this) { result ->
			result.fold(
				{ location ->
					GeoPoint(location).also {
						binding.map.controller.setCenter(it)
						currentPositionMarker.position = it
					}
					binding.map.invalidate()
				},
				{
					Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
				}
			)
		}

		locationClient.currentLocation.observe(this) { result ->
			result.fold(
				{ location ->
					GeoPoint(location).also {
						binding.map.controller.animateTo(it)
						binding.map.controller.setZoom(18.0)
					}
				},
				{
					Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
				}
			)

		}

		binding.btnMyLocation.visibility = View.VISIBLE
		binding.btnMyLocation.setOnClickListener {
			locationClient.getCurrentLocation()
		}

		val mapEventOverlay = MapEventsOverlay(object : MapEventsReceiver {
			override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
				Marker(binding.map).apply {
					setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
					position = p
					title = "$p"
				}.also {
					binding.map.overlays.add(it)
					binding.map.invalidate()
				}

				return true
			}

			override fun longPressHelper(p: GeoPoint): Boolean {
				activity?.let {
					AlertDialog.Builder(it).apply {
						setMessage("Need create new route?")
						setPositiveButton("Ok") { _, _ ->
							GlobalScope.launch { createRouteTo(p) }
						}
						setNegativeButton("cancel") { dialogInterface, _ ->
							dialogInterface.dismiss()
						}
					}
				}?.also {
					it.show()
				}

				return true
			}
		})

//		binding.map.overlays.add(mapEventOverlay)

		initPlaces()

		binding.map.invalidate()
	}

	private fun initPlaces() {
		val poiMarkers = RadiusMarkerClusterer(requireContext())
		val clusterIcon =
			BonusPackHelper.getBitmapFromVectorDrawable(
				requireContext(),
				org.osmdroid.bonuspack.R.drawable.marker_cluster
			)
		poiMarkers.setIcon(clusterIcon)
		binding.map.overlays.add(poiMarkers)

		PlaceProvider.places.map {
			Marker(binding.map).apply {
				position = GeoPoint(it.l, it.w)
				title = it.title
			}.also {
				poiMarkers.add(it)
			}
		}
	}

	@Suppress("RedundantSuspendModifier")
	private suspend fun createRouteTo(targetPoint: GeoPoint) {
		val roadManager: RoadManager = OSRMRoadManager(requireContext(), "adasd")

		val waypoints = ArrayList<GeoPoint>()
		waypoints.add(currentPositionMarker.position)
		waypoints.add(targetPoint)

		val road = roadManager.getRoad(waypoints)

		roadOverlay?.let { binding.map.overlays.remove(it) }
		roadOverlay = RoadManager.buildRoadOverlay(road, Color.BLUE, 10f)
		binding.map.overlays.add(roadOverlay)
		binding.map.invalidate()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		locationClient = LocationClient(requireContext())

		viewModel = ViewModelProvider(this)[MainViewModel::class.java]
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentMainBinding.inflate(inflater, container, false)

		val perms = mutableListOf(
			Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.ACCESS_NETWORK_STATE,
			Manifest.permission.INTERNET
		)

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
			perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
			perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
		} else {
			perms.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
		}

		requestPermissionLauncher.launch(perms.toTypedArray())

		currentPositionMarker = Marker(binding.map).apply {
			setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
			setOnMarkerClickListener { _, _ -> return@setOnMarkerClickListener false }
			icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_current_location)
		}.also {
			binding.map.overlays.add(it)
		}

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()

		_binding = null
	}

	companion object {
		fun newInstance() = MainFragment()
	}
}
