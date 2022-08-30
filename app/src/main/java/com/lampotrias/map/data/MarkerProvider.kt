package com.lampotrias.map.data

import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

object PlaceProvider {
	val places = listOf(
		Place("Часы на Рыночной площади", 55.175560, 21.545397),
		Place("Кирха Калльнингкена", 55.159097, 21.321913),
		Place(
			"Кирха Каукемена",
			55.175047,
			21.546012,
			"https://www.prussia39.ru/phsight/1337171360.jpg"
		),
		Place("Водонапорная башня Хайлигенбайля", 54.461686, 19.941968),
		Place("Памятник шпротам", 54.463736, 19.942261),
		Place("Руины городской стены", 54.459451, 19.937802),
		Place(
			"Захоронение воинов, погибших в годы Первой мировой войны",
			54.457320,
			19.932372
		),
		Place("Крепость Пиллау", 54.643283, 19.886941),
		Place(
			"Водонапорная башня на острове Русском",
			54.638924,
			19.898871
		),
	)
}

class MyMarker(mapView: MapView) : Marker(mapView)

data class Place(
	val title: String,
	val l: Double,
	val w: Double,
	val imageUrl: String = ""
)