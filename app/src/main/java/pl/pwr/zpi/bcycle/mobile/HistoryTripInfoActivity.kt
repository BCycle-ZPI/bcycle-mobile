package pl.pwr.zpi.bcycle.mobile

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_history_trip_info.*
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import pl.pwr.zpi.bcycle.mobile.api.ApiClient
import pl.pwr.zpi.bcycle.mobile.models.Trip
import pl.pwr.zpi.bcycle.mobile.utils.background
import pl.pwr.zpi.bcycle.mobile.utils.showToastError
import pl.pwr.zpi.bcycle.mobile.utils.showToastWarning


class HistoryTripInfoActivity : AppCompatActivity(), OnMyMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var trip: Trip
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var photos: List<String>

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_trip_info)
        val tripID = intent.extras!!.getInt(KEY_TRIP_ID)
        getMaps()
        setListeners()
        ApiClient.tripApi.get(tripID).background().subscribe({
            trip = it.result
            photos = trip.photos
            showInfo(trip)
            showRoute(trip.route, mMap)
            animateTo(
                trip.route[trip.route.size / 2].latitude,
                trip.route[trip.route.size / 2].longitude,
                mMap
            )
        }, {
            showToastError(R.string.prompt_unable_to_load_tripinfo)
        })
    }

    private fun setListeners() {
        backBT.setOnClickListener { finish() }
        photosBT.setOnClickListener {
            if (photos.isNotEmpty()) {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragmentContainer, PhotosFragment(
                        this,
                        photos
                    )
                ).commit()
            } else {
                showToastWarning(R.string.no_photos)
            }
        }
    }

    private fun getMaps() {
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.myMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("CheckResult")
    private fun showInfo(trip: Trip) {
//        ApiClient.groupTripApi.get(trip.groupTripId!!).background().subscribe({
//            titleTV.text = it.result.name
//            participantLV.adapter = ArrayAdapter(this, R.layout.listview_row, it.result.participants)
//        },{})
        startTV.text = trip.started.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
        endTV.text = trip.finished.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
        durationTV.text = getString(
            R.string.time_format,
            (trip.time / 60000).div(60),
            (trip.time / 60000).rem(60)
        )
        distanceTV.text = getString(R.string.distance_format, trip.distance.div(1000))

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }
}