package pl.pwr.zpi.bcycle.mobile

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import pl.pwr.zpi.bcycle.mobile.models.GroupTripPoint
import pl.pwr.zpi.bcycle.mobile.models.TripPoint
import pl.pwr.zpi.bcycle.mobile.models.TripPointTemplate
import kotlin.math.ln

interface OnMyMapReadyCallback : OnMapReadyCallback {

    private fun showMarkers(markers: List<TripPointTemplate>, map: GoogleMap) {
        markers.forEach {
            map.addMarker(MarkerOptions().position(LatLng(it.lat, it.lng)))
        }
    }

    fun displayTripMarkers(markers: List<TripPointTemplate>, map: GoogleMap) {
        showMarkers(markers.subList(1, markers.size - 1), map)
        val startPoint = markers[0]
        val endPoint = markers[markers.size - 1]
        showPoint(startPoint.lat, startPoint.lng, true, map)
        showPoint(endPoint.lat, endPoint.lng, false, map)
    }

    private fun showPoint(
        lat: Double,
        lng: Double,
        startPoint: Boolean,
        map: GoogleMap
    ) {
        val res = if (startPoint) R.drawable.ic_arrow_upward else R.drawable.ic_arrow_downward
        map.addMarker(MarkerOptions().position(LatLng(lat, lng)))
            .setIcon(BitmapDescriptorFactory.fromResource(res))
    }


    fun animateTo(lat: Double, lng: Double, map: GoogleMap, zoom: Float = 11f) {
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), zoom)
        map.animateCamera(cameraUpdate)
    }

    fun showRoute(route: List<TripPoint>, googleMap: GoogleMap) {
        val listOfLatLng = mutableListOf<LatLng>()
        route.forEach { x -> listOfLatLng.add(LatLng(x.latitude, x.longitude)) }
        val polyline1: Polyline = googleMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .width(MAP_POLYLINE_WIDTH)
                .addAll(listOfLatLng)
        )

    }
}
