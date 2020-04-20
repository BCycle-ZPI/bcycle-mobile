package pl.pwr.zpi.bcycle.mobile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
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
import com.google.android.gms.maps.model.*
import com.google.maps.android.ui.IconGenerator
import com.yarolegovich.lovelydialog.LovelyChoiceDialog
import kotlinx.android.synthetic.main.activity_trip_creation_map.*
import pl.pwr.zpi.bcycle.mobile.utils.showToast

class TripCreationMapActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener,
    LocationListener {

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var lastLocation: Location
    private var map: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val markers = mutableListOf<Marker>()
    private var locationManager: LocationManager? = null
    private var markerStartPoint: Marker? = null
    private var markerFinishPoint: Marker? = null

    companion object {
        private val LOCATION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_creation_map)
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.myMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        setListeners()
    }

    private fun setListeners() {
        bt_next.setOnClickListener {
            //todo
        }
    }


    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        map?.setOnMarkerClickListener(this)

        handleMapGestures()

        //permission for getting location
        getCurrentLocation()

        map?.setOnMapLongClickListener {
            val markerOpt = MarkerOptions().position(it)
            val mark = map?.addMarker(markerOpt)
            mark?.isDraggable = true
            markers.add(mark!!)
        }
    }

    private fun handleMapGestures() {
        val mapSettings = map?.uiSettings
        mapSettings?.isZoomGesturesEnabled = true
        mapSettings?.isScrollGesturesEnabled = true
    }

    override fun onLocationChanged(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude);
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 13f);
        map?.animateCamera(cameraUpdate);
    }

    override fun onMarkerClick(marker: Marker?): Boolean { //todo
        val array = arrayListOf<String>("set as start point","set as end point", "remove it")
        LovelyChoiceDialog(this)
            .setTopColorRes(R.color.violet)
            .setTitle(resources.getString(R.string.prompt_marker_what_to_do))
            .setIcon(R.drawable.bike_icon)
            .setItemsMultiChoice(
                array,
                LovelyChoiceDialog.OnItemsSelectedListener<String>() { positions: List<Int>, items: List<String> ->
                    if(items.contains(array[0], array[2]) || items.contains(array[1], array[2])){
                        showToast("You cannot set the point as start/end point of your trip and remove it.")
                    }
                    else if(items.contains(array[0]) && items.contains(array[1])){
                        markerStartPoint?.setIcon(null)
                        markerFinishPoint?.setIcon(null)

                        markerStartPoint = marker
                        markerFinishPoint = marker
                        marker?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bike_black))
                        marker?.title = "start point & end point"
                        marker?.showInfoWindow()
                    }
                    else if(items.contains(array[0])){
                        markerStartPoint?.setIcon(null)
                        marker?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bike_black))
                        markerStartPoint = marker
                        marker?.title = "start point"
                        marker?.showInfoWindow()
                    }
                    else if(items.contains(array[1])){
                        markerFinishPoint?.setIcon(null)
                        marker?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bike_black))
                        markerFinishPoint = marker
                        marker?.title= "end point"
                        marker?.showInfoWindow()
                    }
                    else if(items.contains(array[2])){
                        marker?.remove()
                    }
                    updateStartAndFinishMarkers()
                    Toast.makeText(this@TripCreationMapActivity, items.toString() + positions.toString(), Toast.LENGTH_LONG).show()
                })
            .setConfirmButtonText(R.string.confirm)
            .show()
        return true
    }

    private fun updateStartAndFinishMarkers(){
        //todo
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
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
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
                    Toast.makeText(
                        this,
                        R.string.location_denied_prompt,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}

private fun <E> Collection<E>.contains(vararg ts: E): Boolean {
    for(single in ts){
        if(!contains(single)) return false
    }
    return true
}
