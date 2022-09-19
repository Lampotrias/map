package com.lampotrias.map.tools.bottomsheet

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

class BottomSheetHelper {

	fun show(manager: FragmentManager, dialog: DialogFragment) {
		if (manager.isStateSaved || manager.isDestroyed) {
			return
		}
		dismiss(manager)
		dialog.show(manager, BOTTOM_SHEET_FRAGMENT_TAG)
	}

	fun dismiss(manager: FragmentManager) {
		if (manager.isStateSaved || manager.isDestroyed) {
			return
		}
		val dialog = manager.findFragmentByTag(BOTTOM_SHEET_FRAGMENT_TAG) as DialogFragment?
		dialog?.dismiss()
	}

	companion object {
		private const val BOTTOM_SHEET_FRAGMENT_TAG = "bottom_sheet_fragment"
	}
}