package pl.pwr.zpi.bcycle.mobile

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import pl.pwr.zpi.bcycle.mobile.models.GroupTripPoint
import pl.pwr.zpi.bcycle.mobile.models.TripPoint
import pl.pwr.zpi.bcycle.mobile.models.TripPointTemplate

interface OnMyMapReadyCallback : OnMapReadyCallback{

    fun showMarkersAndAnimateThere(markers:List<TripPointTemplate>, map:GoogleMap){
        var firstIter = true
        markers.forEach{
            if(it is GroupTripPoint){
                map.addMarker(MarkerOptions().position(LatLng(it.latitude, it.longitude)))
                if(firstIter){
                    animateTo(it.latitude, it.longitude, map)
                    firstIter = false
                }
            }
            else if(it is TripPoint){
                map.addMarker(MarkerOptions().position(LatLng(it.latitude, it.longitude)))
                if(firstIter){
                    animateTo(it.latitude, it.longitude, map)
                    firstIter = false
                }
            }
        }

    }

//    fun showPoint(, startPoint:bool){}


    private fun animateTo(lat:Double, lng:Double, map:GoogleMap){
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 7f)
        map.animateCamera(cameraUpdate)
    }
}
