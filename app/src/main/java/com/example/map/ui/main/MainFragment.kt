package com.example.map.ui.main

import android.Manifest
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.map.R
import com.example.map.databinding.FragmentMainBinding
import com.example.map.location.LocationClient
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class MainFragment : Fragment() {

	private lateinit var viewModel: MainViewModel
	private var _binding: FragmentMainBinding? = null
	private val binding get() = _binding!!

	private lateinit var locationClient: LocationClient
	private lateinit var currentPositionMarker: Marker

	private val requestPermissionLauncher =
		registerForActivityResult(
			ActivityResultContracts.RequestMultiplePermissions()
		) { resultMap ->
			val deniedPerms = resultMap.filter { entry -> entry.value == false }
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

		locationClient.lastLocation.observe(this) { location ->
			GeoPoint(location).also {
				binding.map.controller.setCenter(it)
				currentPositionMarker.position = it
			}
			binding.map.invalidate()
		}
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
