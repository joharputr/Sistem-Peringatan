package com.example.systemperingatan.API.Pojo.Route

data class StepsItem(
	val duration: Duration? = null,
	val startLocation: StartLocation? = null,
	val distance: Distance? = null,
	val travelMode: String? = null,
	val htmlInstructions: String? = null,
	val endLocation: EndLocation? = null,
	val maneuver: String? = null,
	val polyline: Polyline? = null
)
