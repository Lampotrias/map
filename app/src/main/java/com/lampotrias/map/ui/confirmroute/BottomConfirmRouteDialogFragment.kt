package com.lampotrias.map.ui.confirmroute

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lampotrias.map.databinding.FragmentItemListDialogListDialogBinding
import org.osmdroid.bonuspack.routing.Road

const val ROAD_ARG_NAME = "road"

class BottomConfirmRouteDialogFragment : BottomSheetDialogFragment() {

	private var _binding: FragmentItemListDialogListDialogBinding? = null

	// This property is only valid between onCreateView and
	// onDestroyView.
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {

		_binding = FragmentItemListDialogListDialogBinding.inflate(inflater, container, false)
		return binding.root

	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		val road = arguments?.getParcelable<Road>(ROAD_ARG_NAME) ?: return
		binding.text.text = road.getLengthDurationText(requireContext(), -1)
	}


	companion object {

		// TODO: Customize parameters
		fun newInstance(road: Road): BottomConfirmRouteDialogFragment =
			BottomConfirmRouteDialogFragment().apply {
				arguments = Bundle().apply {
					putParcelable(ROAD_ARG_NAME, road)
				}
			}

	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}