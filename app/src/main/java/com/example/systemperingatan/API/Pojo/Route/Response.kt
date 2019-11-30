package com.example.systemperingatan.API.Pojo.Route

data class Response(
	val routes: List<RoutesItem?>? = null,
	val geocodedWaypoints: List<GeocodedWaypointsItem?>? = null,
	val status: String? = null
)
