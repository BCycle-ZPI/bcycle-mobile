package pl.pwr.zpi.bcycle.mobile

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import pl.pwr.zpi.bcycle.mobile.api.ApiClient
import pl.pwr.zpi.bcycle.mobile.models.Trip
import pl.pwr.zpi.bcycle.mobile.utils.background
import pl.pwr.zpi.bcycle.mobile.utils.showToastError


class HistoryTripInfoActivity : AppCompatActivity(), OnMyMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var trip: Trip
    private lateinit var mapFragment: SupportMapFragment

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_trip_info)
        val tripID = intent.extras.getInt(KEY_TRIP_ID)
        getMaps()
        ApiClient.tripApi.get(tripID).background().subscribe({
            trip = it.result
            showRoute(trip.route, mMap)
            animateTo(trip.route[trip.route.size/2].latitude,trip.route[trip.route.size/2].longitude, mMap)
        }, {
            showToastError(R.string.prompt_unable_to_load_tripinfo)
        })
    }

    private fun getMaps() {
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.myMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

    }
}