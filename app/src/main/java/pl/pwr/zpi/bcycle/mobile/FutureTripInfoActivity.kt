package pl.pwr.zpi.bcycle.mobile

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_future_trip_info.*
import pl.pwr.zpi.bcycle.mobile.api.ApiClient
import pl.pwr.zpi.bcycle.mobile.models.GroupTrip
import pl.pwr.zpi.bcycle.mobile.utils.background
import pl.pwr.zpi.bcycle.mobile.utils.showToastError

class FutureTripInfoActivity : AppCompatActivity(), OnMyMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var trip:GroupTrip
    private lateinit var mapFragment: SupportMapFragment


    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_future_trip_info)
        getMaps()
        val tripID = intent!!.extras.getInt(KEY_TRIP_ID)
        ApiClient.groupTripApi.get(tripID).background().subscribe({
            trip = it.result
            setFields(trip)
        }, {
            showToastError(R.string.prompt_unable_to_load_tripinfo)
        })
    }

    private fun getMaps(){
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.myMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setFields(trip:GroupTrip) {
        tripnameTV.text = trip.name
        val markers = trip.route
        val participants = trip.participants
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, participants)
        participantsLV.adapter = adapter
        showMarkersAndAnimateThere(markers, map)
       // showPoint(markers[0], true)
       // showPoint(markers[markers.size-1], false)
    }

    override fun onMapReady(p0: GoogleMap?) {
        map = p0!!
    }
}
