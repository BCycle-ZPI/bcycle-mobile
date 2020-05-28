package pl.pwr.zpi.bcycle.mobile.models

import com.google.android.gms.maps.model.LatLng

abstract class TripPointTemplate {
    abstract val lat: Double
    abstract val lng: Double
    fun asLatLng(): LatLng = LatLng(lat, lng)
}
