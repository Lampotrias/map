package com.lampotrias.map.ui.main

import android.Manifest
import android.app.AlertDialog
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.facebook.drawee.view.SimpleDraweeView
import com.lampotrias.map.R
import com.lampotrias.map.data.MyMarker
import com.lampotrias.map.databinding.FragmentMainBinding
import com.lampotrias.map.location.LocationClient
import com.lampotrias.map.tools.bottomsheet.BottomSheetHelper
import com.lampotrias.map.ui.confirmroute.BottomConfirmRouteDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.bonuspack.utils.BonusPackHelper
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView.getTileSystem
import org.osmdroid.views.overlay.IconOverlay.ANCHOR_CENTER
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import kotlin.math.roundToInt


@OptIn(DelicateCoroutinesApi::class)
@AndroidEntryPoint
class MainFragment : Fragment() {

	private val viewModel by viewModels<MainViewModel>()

	private var _binding: FragmentMainBinding? = null
	private val binding get() = _binding!!

	private lateinit var locationClient: LocationClient
	private lateinit var currentPositionMarker: Marker
	private var currentRoad: CurrentRoute? = null
	private val bottomSheetHelper = BottomSheetHelper()

	private val requestPermissionLauncher =
		registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultMap ->
			Log.e("adadasda", resultMap.toString())
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
//			overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS);
			setTileSource(TileSourceFactory.MAPNIK)
			setMultiTouchControls(true)
			isTilesScaledToDpi = true
			minZoomLevel = 2.0
			maxZoomLevel = 21.0
			isVerticalMapRepetitionEnabled = false
			setScrollableAreaLimitLatitude(
				getTileSystem().maxLatitude,
				-getTileSystem().maxLatitude,
				0
			)
			controller.setZoom(18.0)
		}

		enableRotationMap()
		enableCompass()

		enableMyLocation()

		enableGestureClicks()
		initPlaces()

		enableTrackStatus()

		enableLogView()

		binding.map.invalidate()

		subscribeEvents()

		locationClient.getLastLocation()
	}

	private fun enableMyLocation() {
		binding.btnMyLocation.visibility = View.VISIBLE
		binding.btnMyLocation.setOnClickListener {
			locationClient.getCurrentLocation()
		}
	}

	private fun enableTrackStatus() {
		binding.trackStatus.setBackgroundColor(Color.RED)
		binding.trackStatus.setOnClickListener {
			if ((it.background as? ColorDrawable)?.color == Color.GREEN) {
				viewModel.stopLocationUpdates()
				viewModel.stopSpeedUpdates()
			} else {
				viewModel.startLocationUpdates()
				viewModel.startSpeedUpdates()
			}
		}
	}

	private fun enableLogView() {
		binding.logView.movementMethod = ScrollingMovementMethod()

		binding.logsStatus.setBackgroundColor(Color.RED)
		binding.logsStatus.setOnClickListener {
			if ((it.background as? ColorDrawable)?.color == Color.RED) {
				binding.logView.visibility = View.VISIBLE
				binding.logsStatus.setBackgroundColor(Color.GREEN)
			} else {
				binding.logView.visibility = View.GONE
				binding.logsStatus.setBackgroundColor(Color.RED)
			}
		}
	}

	private fun enableCompass() {
		val mCompassOverlay =
			CompassOverlay(context, InternalCompassOrientationProvider(context), binding.map)
		mCompassOverlay.enableCompass()
		binding.map.overlays.add(mCompassOverlay)
	}

	private fun subscribeEvents() {

		locationClient.lastLocation.observe(this) { result ->
			result.fold(
				{ location ->

					appendToLog("last: $location")

					if (location == null) {
						Toast.makeText(requireContext(), "Location is null", Toast.LENGTH_SHORT)
							.show()
						return@observe
					}

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

					appendToLog("current: $location")

					if (location == null) {
						Toast.makeText(requireContext(), "Location is null", Toast.LENGTH_SHORT)
							.show()
						return@observe
					}

					GeoPoint(location).also {
						binding.map.controller.animateTo(it)
						binding.map.controller.setZoom(18.0)
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

		viewLifecycleOwner.lifecycleScope.launch {
			// repeatOnLifecycle launches the block in a new coroutine every time the
			// lifecycle is in the STARTED state (or above) and cancels it when it's STOPPED.
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
				// Trigger the flow and start listening for values.
				// This happens when lifecycle is STARTED and stops
				// collecting when the lifecycle is STOPPED

				launch {
					viewModel.receivingLocationUpdates.collect { enabled ->
						if (enabled) {
							binding.trackStatus.setBackgroundColor(Color.GREEN)
						} else {
							binding.trackStatus.setBackgroundColor(Color.RED)
							binding.speedKmc.text = ""
							binding.speedMts.text = ""
						}
					}
				}

				launch {
					viewModel.speedUpdates.collect {
						binding.speedKmc.text = "${(it * 3.6).roundToInt()} km"
						binding.speedMts.text = "${it.roundToInt()} ms"
					}
				}

				launch {
					viewModel.locationList.stateIn(viewLifecycleOwner.lifecycleScope)
						.collect { locations ->
							locations.firstOrNull()?.let { newPoint ->
								GeoPoint(newPoint.latitude, newPoint.longitude).also {
									binding.map.controller.animateTo(it)
									currentPositionMarker.position = it
									requireActivity().runOnUiThread {
										Toast.makeText(
											requireContext(),
											newPoint.toString(),
											Toast.LENGTH_SHORT
										).show()
									}
								}
								appendToLog("list: $newPoint")
								binding.map.invalidate()
							}
						}
				}
			}
		}
	}

	private fun enableGestureClicks() {
		val mapEventOverlay = MapEventsOverlay(object : MapEventsReceiver {
			override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
//				Marker(binding.map).apply {
//					setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
//					position = p
//					title = "$p"
//				}.also {
//					binding.map.overlays.add(it)
//					binding.map.invalidate()
//				}

				return false
			}

			override fun longPressHelper(p: GeoPoint): Boolean {
				activity?.let {
					AlertDialog.Builder(it).apply {
						setMessage("Need create new route?")
						setPositiveButton("Ok") { _, _ ->
							GlobalScope.launch {
								withContext(Dispatchers.IO) {
									createRouteTo(p)
								}
							}
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
		binding.map.overlays.add(mapEventOverlay)
	}

	private fun enableRotationMap() {
		val mRotationGestureOverlay = RotationGestureOverlay(binding.map)
		mRotationGestureOverlay.isEnabled = true
		binding.map.overlays.add(mRotationGestureOverlay)
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

		viewModel.savedItems.map {
			MyMarker(binding.map).apply {
				position = GeoPoint(it.l, it.w)
				title = it.title
				icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_poi_marker, null)
				setOnMarkerClickListener { marker, mapView ->
					GlobalScope.launch {
						withContext(Dispatchers.Main) {
							val myCustomInfoWindow = CustomInfoView(mapView)
							setInfoWindowAnchor(ANCHOR_CENTER, ANCHOR_CENTER)
							myCustomInfoWindow.view.findViewById<SimpleDraweeView>(R.id.drawee_image)
								.apply {
									if (it.imageUrl.isNotEmpty()) {
										setImageURI(it.imageUrl)
									}
								}
							marker.setInfoWindow(myCustomInfoWindow)
							marker.showInfoWindow()
						}
					}
					return@setOnMarkerClickListener false
				}

			}.also {
				poiMarkers.add(it)
			}
		}
	}

	private fun appendToLog(message: String) {
		requireActivity().runOnUiThread {
			binding.logView.append("$message \n")
		}
	}

	@Suppress("RedundantSuspendModifier")
	private suspend fun createRouteTo(targetPoint: GeoPoint) {
		val roadManager: RoadManager = OSRMRoadManager(requireContext(), "adasd")

		val waypoints = ArrayList<GeoPoint>()
		waypoints.add(currentPositionMarker.position)
		waypoints.add(targetPoint)

		val road = roadManager.getRoad(waypoints)

		currentRoad?.let {
			binding.map.overlays.remove(it.routeOverlay)
			binding.map.overlays.removeAll(it.points)
		}

		val points = road.mNodes.map {
			val marker = Marker(binding.map).apply {
				setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
				position = it.mLocation
				image = ContextCompat.getDrawable(requireContext(), R.drawable.ic_cluster)
			}
			marker
		}

		currentRoad = CurrentRoute(
			road,
			RoadManager.buildRoadOverlay(road, Color.BLUE, 20f),
			points
		)

		binding.map.overlays.add(currentRoad!!.routeOverlay)
		binding.map.overlays.addAll(points)

		val newFragment = BottomConfirmRouteDialogFragment.newInstance(road)
		bottomSheetHelper.show(parentFragmentManager, newFragment)

		binding.map.invalidate()
	}

	data class CurrentRoute(
		val road: Road,
		val routeOverlay: Overlay,
		val points: List<Marker>
	)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		locationClient = LocationClient(requireContext())
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentMainBinding.inflate(inflater, container, false)

		val perms = mutableListOf(
			Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.ACCESS_NETWORK_STATE,
			Manifest.permission.INTERNET
		)

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
			perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
			perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
		} else {
			perms.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
		}

		val locationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			requestPermissionLauncher.launch(perms.toTypedArray())
		} else {
			val builder = activity?.let {
				AlertDialog.Builder(it)
			}?.also {
				it.setTitle("Gps provider was disabled")
				it.setMessage("Please enable it")
				it.setPositiveButton("Go to settings") { _, _ ->
					requireActivity().startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
				}
			}
			builder?.show()
		}

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
