package com.example.map.ui.main

import android.Manifest
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.map.databinding.FragmentMainBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

class MainFragment : Fragment() {

	private lateinit var viewModel: MainViewModel
	private var _binding: FragmentMainBinding? = null
	private val binding get() = _binding!!

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
		binding.map.setTileSource(TileSourceFactory.MAPNIK)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
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
		}

		requestPermissionLauncher.launch(perms.toTypedArray())

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
