package pl.pwr.zpi.bcycle.mobile

import android.Manifest
import android.content.Context
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.yarolegovich.lovelydialog.LovelyChoiceDialog
import com.yarolegovich.lovelydialog.LovelyStandardDialog
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_trip_creation_map.*
import org.threeten.bp.ZonedDateTime
import pl.pwr.zpi.bcycle.mobile.api.ApiClient
import pl.pwr.zpi.bcycle.mobile.models.GroupTrip
import pl.pwr.zpi.bcycle.mobile.models.GroupTripPoint
import pl.pwr.zpi.bcycle.mobile.models.TripPoint
import pl.pwr.zpi.bcycle.mobile.models.UserInfo
import pl.pwr.zpi.bcycle.mobile.utils.showToast
import pl.pwr.zpi.bcycle.mobile.utils.showToastError
import java.security.acl.Group

class TripCreationMapActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener,
    LocationListener {

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var lastLocation: Location
    private var map: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var myMarkers = mutableListOf<Marker>()
    private var locationManager: LocationManager? = null
    private var markerStartPoint: Marker? = null
    private var markerFinishPoint: Marker? = null
    // region intent.extra data
    private lateinit var savedStartDate: ZonedDateTime
    private lateinit var savedEndDate: ZonedDateTime
    private lateinit var savedName: String
    private lateinit var savedDesc: String
    // endregion intent.extra data
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    companion object {
        private val LOCATION_REQUEST_CODE = 101
        private val MARKERS_LIST_KEY = "MARKERSLIST"
        private val START_POINT_KEY = "STARTPOINTKEY"
        private val FINISH_POINT_KEY = "FINISHPOINTKEY"

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
    }

    private fun getSavedData() {
        val extras = intent.extras;
        savedDesc = extras.getString(TripCreationActivity.DESCRIPTION_KEY)!!
        savedName = extras.getString(TripCreationActivity.NAME_KEY)!!
        savedEndDate = extras.getSerializable(TripCreationActivity.END_DATE_KEY)!! as ZonedDateTime
        savedStartDate =
            extras.getSerializable(TripCreationActivity.START_DATE_KEY)!! as ZonedDateTime
    }

    private fun setListeners() {
        bt_next.setOnClickListener {
            LovelyStandardDialog(this)
                .setTopColorRes(R.color.violet)
                .setTitle(resources.getString(R.string.prompt_is_trip_done))
                .setIcon(R.drawable.bike_icon)
                .setPositiveButton(R.string.yes) {
                    val photoUrl = FirebaseStorage.getInstance()
                        .reference.child(auth.currentUser!!.uid + ".png").toString()
                    val route = createMarkersList()
                    ApiClient.groupTripApi.create(
                        GroupTrip(
                            auth.currentUser?.uid?.toInt(), savedName, savedDesc,
                            UserInfo(
                                auth.currentUser?.uid!!,
                                auth.currentUser?.displayName!!,
                                auth.currentUser?.email!!,
                                photoUrl
                            ), null, savedStartDate, savedEndDate, route, null
                        )
                    )
                    //todo
                    showToast("aaaaa")
                }
                .setPositiveButtonColorRes(R.color.green)
                .setNegativeButton(R.string.no, {})
                .setNegativeButtonColorRes(R.color.red)
                .show()
        }
        bt_show_start.setOnClickListener {
            if (markerStartPoint != null) {
                markerStartPoint?.showInfoWindow()
                map?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        markerStartPoint?.position,
                        12f
                    )
                )
            }
        }

        bt_show_end.setOnClickListener {
            if (markerFinishPoint != null) {
                markerFinishPoint?.showInfoWindow()
                map?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        markerFinishPoint?.position,
                        12f
                    )
                )
            }
        }
    }

    private fun createMarkersList(): MutableList<GroupTripPoint> {
        val list = mutableListOf<GroupTripPoint>()
        list.add(
            GroupTripPoint(
                markerStartPoint!!.position.latitude,
                markerStartPoint!!.position.longitude,
                null
            )
        )
        for (marker in myMarkers) {
            if (marker != markerStartPoint && marker != markerFinishPoint) {
                list.add(GroupTripPoint(marker.position.latitude, marker.position.longitude, null))
            }
        }
        list.add(
            GroupTripPoint(
                markerFinishPoint!!.position.latitude,
                markerFinishPoint!!.position.longitude,
                null
            )
        )
        return list
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        map?.setOnMarkerClickListener(this)

        handleMapGestures()
        getCurrentLocation()

        map?.setOnMapLongClickListener {
            val markerOpt =
                MarkerOptions().position(it).icon(BitmapDescriptorFactory.defaultMarker())
            val mark = map?.addMarker(markerOpt)
            mark?.isDraggable = true

            // myMarkers.add(GroupTripPoint(markerOpt.position.latitude, markerOpt.position.longitude, null))
            myMarkers.add(mark!!)
        }
    }

    private fun handleMapGestures() {
        val mapSettings = map?.uiSettings
        mapSettings?.isZoomGesturesEnabled = true
        mapSettings?.isScrollGesturesEnabled = true
    }

    // region markerClickListener
    override fun onMarkerClick(marker: Marker?): Boolean {
        val array = arrayListOf<String>(
            getString(R.string.set_as_start_point), getString(R.string.set_as_end_point), getString(
                R.string.remove_it
            )
        )
        LovelyChoiceDialog(this)
            .setTopColorRes(R.color.violet)
            .setTitle(resources.getString(R.string.prompt_marker_what_to_do))
            .setIcon(R.drawable.bike_icon)
            .setItemsMultiChoice(
                array
            ) { _: List<Int>, items: List<String> ->
                if (items.contains(array[0], array[2]) || items.contains(array[1], array[2])) {
                    showToastError((R.string.warning_cant_set_and_delete_marker))
                } else if (items.contains(array[0]) && items.contains(array[1])) {
                    markerStartPoint?.setIcon(null)
                    markerFinishPoint?.setIcon(null)
                    markerStartPoint = marker
                    markerFinishPoint = marker
                    marker?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bike_black))
                    marker?.title = getString(R.string.start_and_end_point)
                    marker?.showInfoWindow()
                } else if (items.contains(array[0])) {
                    markerStartPoint?.setIcon(null)
                    marker?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bike_black))
                    markerStartPoint = marker
                    marker?.title = getString(R.string.start_point)
                    marker?.showInfoWindow()
                } else if (items.contains(array[1])) {
                    markerFinishPoint?.setIcon(null)
                    marker?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bike_black))
                    markerFinishPoint = marker
                    marker?.title = getString(R.string.end_point)
                    marker?.showInfoWindow()
                } else if (items.contains(array[2])) {
                    if (marker == markerStartPoint) markerStartPoint = null
                    if (marker == markerFinishPoint) markerFinishPoint = null
                    marker?.remove()
                }
            }
            .setConfirmButtonText(R.string.confirm)
            .show()
        return true
    }

    // endregion markerClickListener

    // region requesting permission region

    override fun onLocationChanged(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12f)
        map?.animateCamera(cameraUpdate)
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
        if (map != null) {
            val permission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            if (permission == PackageManager.PERMISSION_GRANTED) {
                map?.isMyLocationEnabled = true
                zoomToCurrentLocation()
            }

        } else {
            requestPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                LOCATION_REQUEST_CODE
            )
        }
    }

    private fun zoomToCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
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

private fun <E> Collection<E>.contains(vararg ts: E): Boolean {
    for (single in ts) {
        if (!contains(single)) return false
    }
    return true
}