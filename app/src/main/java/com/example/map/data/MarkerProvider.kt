package com.example.map.data

object PlaceProvider {
	val places = listOf<Place>(
		Place("Часы на Рыночной площади", 55.175560, 21.545397),
		Place("Кирха Каукемена", 55.175047, 21.546012)
	)
}

data class Place(
	val title: String,
	val l: Double,
	val w: Double,
	val imageUrl: String = ""
)