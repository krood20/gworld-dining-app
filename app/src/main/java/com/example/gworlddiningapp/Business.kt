package com.example.gworlddiningapp

import com.google.android.gms.maps.model.LatLng

data class Business(
    val business_id: String,
    val name: String,
    val url: String,
    val lat: Double,
    val lng: Double,
    val address: String,
    val rating: Double,
    var gworld: Boolean
)