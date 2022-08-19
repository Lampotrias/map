package com.example.map.ui.main

import com.example.map.R
import org.osmdroid.views.MapView

import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow


class CustomInfoView(mapView: MapView?) :
	MarkerInfoWindow(R.layout.info_layout, mapView)