package com.example.systemperingatan.API.Pojo.Route

data class LegsItem(
	val duration: Duration? = null,
	val startLocation: StartLocation? = null,
	val distance: Distance? = null,
	val startAddress: String? = null,
	val endLocation: EndLocation? = null,
	val endAddress: String? = null,
	val viaWaypoint: List<Any?>? = null,
	val steps: List<StepsItem?>? = null,
	val trafficSpeedEntry: List<Any?>? = null
)
