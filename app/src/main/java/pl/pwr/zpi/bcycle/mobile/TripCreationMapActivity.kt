package pl.pwr.zpi.bcycle.mobile

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.yarolegovich.lovelydialog.LovelyChoiceDialog
import com.yarolegovich.lovelydialog.LovelyStandardDialog
import kotlinx.android.synthetic.main.activity_trip_creation_map.*
import org.threeten.bp.ZonedDateTime
import pl.pwr.zpi.bcycle.mobile.api.ApiClient
import pl.pwr.zpi.bcycle.mobile.models.GroupTrip
import pl.pwr.zpi.bcycle.mobile.models.GroupTripPoint
import pl.pwr.zpi.bcycle.mobile.utils.background
import pl.pwr.zpi.bcycle.mobile.utils.showToast
import pl.pwr.zpi.bcycle.mobile.utils.showToastError

class TripCreationMapActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener,
    LocationListener {

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var lastLocation: Location
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var myMarkers = mutableListOf<Marker>()
    private lateinit var locationManager: LocationManager
    private var markerStartPoint: Marker? = null
    private var markerFinishPoint: Marker? = null
    // region intent.extra data
    private lateinit var savedStartDate: ZonedDateTime
    private lateinit var savedEndDate: ZonedDateTime
    private lateinit var savedName: String
    private lateinit var savedDesc: String
    // endregion intent.extra data

    companion object {
        private const val LOCATION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_creation_map)
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.myMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        setListeners()
        getSavedData()
        if(!mapCreationGuideShowed){
            Snackbar.make(layout, getString(R.string.prompt_guide_map_start), Snackbar.LENGTH_LONG).show()
            mapCreationGuideShowed = true
        }
    }

    private fun getSavedData() {
        val extras = intent.extras!!
        savedDesc = extras.getString(DESCRIPTION_KEY)!!
        savedName = extras.getString(NAME_KEY)!!
        savedEndDate = extras.getSerializable(END_DATE_KEY)!! as ZonedDateTime
        savedStartDate =
            extras.getSerializable(START_DATE_KEY)!! as ZonedDateTime
    }

    private fun setListeners() {
        nextBT.setOnClickListener {
            showDialog()
        }
        zoomToStartBT.setOnClickListener {
            animateToMarker(MarkerType.START)
        }

        zoomToEndBT.setOnClickListener {
            animateToMarker(MarkerType.END)
        }
    }

    private fun showDialog(){
        LovelyStandardDialog(this)
            .setTopColorRes(R.color.colorAccent)
            .setTitle(resources.getString(R.string.prompt_is_trip_done))
            .setIcon(R.drawable.bike_icon)
            .setPositiveButton(R.string.yes) {
                if(isRequiredDataGiven()){
                    val route = createMarkersList()
                    ApiClient.groupTripApi.create(
                        GroupTrip(
                            null, savedName, savedDesc,
                            null,
                            null, savedStartDate, savedEndDate, route, null
                        )
                    ).background().subscribe({
                        showToast(getString(R.string.prompt_trid_added_successfully))
                        startActivity(
                            Intent(
                                this,
                                MainActivity::class.java
                            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        )
                        finish()
                    }, { })
                }
                else{
                    showToastError(R.string.error_missing_start_or_end_marker)
                }
            }
            .setPositiveButtonColorRes(R.color.green)
            .setNegativeButton(R.string.no,{})
            .setNegativeButtonColorRes(R.color.red)
            .show()
    }

    enum class MarkerType{START, END}
    private fun animateToMarker(markerType:MarkerType) {
        val marker = if (markerType == MarkerType.START) markerStartPoint else markerFinishPoint
        if (marker != null) {
            marker.showInfoWindow()
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    marker.position,
                    12f
                )
            )
        }
    }

    private fun isRequiredDataGiven(): Boolean {
        return markerStartPoint!=null && markerFinishPoint!=null
    }

    private fun createMarkersList(): MutableList<GroupTripPoint> {
        val list = mutableListOf<GroupTripPoint>()
        list.add(
            GroupTripPoint(
                markerStartPoint!!.getLat(),
                markerStartPoint!!.getLon(),
                null
            )
        )
        for (marker in myMarkers) {
            if (marker != markerStartPoint && marker != markerFinishPoint) {
                list.add(GroupTripPoint(marker.getLat(), marker.getLon(), null))
            }
        }
        list.add(
            GroupTripPoint(
                markerFinishPoint!!.getLat(),
                markerFinishPoint!!.getLon(),
                null
            )
        )
        return list
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap!!
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        map.setOnMarkerClickListener(this)

        handleMapGestures()
        getCurrentLocation()

        map.setOnMapLongClickListener {
            if(markerOptionsGuideShowed)
            {
                Snackbar.make(layout, getString(R.string.prompt_guide_map_marker_options), Snackbar.LENGTH_LONG).show()
                markerOptionsGuideShowed = true
            }
            val markerOpt =
                MarkerOptions().position(it).icon(BitmapDescriptorFactory.defaultMarker())
            val mark = map.addMarker(markerOpt)
            mark?.isDraggable = true
            myMarkers.add(mark!!)
        }
    }

    private fun handleMapGestures() {
        val mapSettings = map.uiSettings
        mapSettings?.isZoomGesturesEnabled = true
        mapSettings?.isScrollGesturesEnabled = true
    }

    // region markerClickListener
    override fun onMarkerClick(marker: Marker?): Boolean {
        val array = arrayListOf<String>(
            getString(R.string.set_as_start_point), getString(R.string.set_as_end_point),getString(
                R.string.set_as_start_and_end_point
            ), getString(
                R.string.remove_it
            )
        )
        LovelyChoiceDialog(this)
            .setTopColorRes(R.color.colorAccent)
            .setTitle(resources.getString(R.string.prompt_marker_what_to_do))
            .setIcon(R.drawable.bike_icon)
            .setItems(
                array
            ) { pos: Int, _: String ->
                when (pos) {
                    0 -> {
                        markerStartPoint?.setIcon(null)
                        marker?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow_upward))
                        markerStartPoint = marker
                        marker?.title = getString(R.string.start_point)
                        marker?.showInfoWindow()
                    }
                    1 -> {
                        markerFinishPoint?.setIcon(null)
                        marker?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow_downward))
                        markerFinishPoint = marker
                        marker?.title = getString(R.string.end_point)
                        marker?.showInfoWindow()
                    }
                    2 -> {
                        markerStartPoint?.setIcon(null)
                        markerFinishPoint?.setIcon(null)
                        markerStartPoint = marker
                        markerFinishPoint = marker
                        marker?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bike_black))
                        marker?.title = getString(R.string.start_and_end_point)
                        marker?.showInfoWindow()
                    }
                    3 -> {
                        if (marker == markerStartPoint) markerStartPoint = null
                        if (marker == markerFinishPoint) markerFinishPoint = null
                        marker?.remove()
                    }
                }
            }
            .show()
        return true
    }

    // endregion markerClickListener

    // region requesting permission region

    override fun onLocationChanged(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12f)
        map.animateCamera(cameraUpdate)
    }

    private fun requestPermission(
        permissionType: String,
        requestCode: Int
    ) {

        ActivityCompat.requestPermissions(
            this,
            arrayOf(permissionType), requestCode
        )
    }

    private fun getCurrentLocation() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permission == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
            zoomToCurrentLocation()
        }

    }

    private fun zoomToCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {

        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] !=
                    PackageManager.PERMISSION_GRANTED
                ) {
                    showToastError(
                        R.string.location_denied_prompt
                    )

                }
            }
        }
    }
    // endregion requesting permission region
}

private fun Marker.getLat(): Double = position.latitude
private fun Marker.getLon(): Double = position.longitude

fun <E> Collection<E>.contains(vararg ts: E): Boolean {
    for (single in ts) {
        if (!contains(single)) return false
    }
    return true
}