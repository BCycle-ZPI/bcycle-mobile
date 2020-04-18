package pl.pwr.zpi.bcycle.mobile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class TripCreationMapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    LocationListener{

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var lastLocation: Location
    private var map: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val markers = mutableListOf<MarkerOptions>()
    private var locationManager:LocationManager? = null

    companion object{
        private val LOCATION_REQUEST_CODE = 101

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_creation_map)
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.myMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        actionBar?.hide()

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        map?.setOnMarkerClickListener(this)

        handleMapGestures()

        //permission for getting location
        getCurrentLocation()

        map?.setOnMapLongClickListener {
            val marker = MarkerOptions().position(it)
            map?.addMarker(marker)
            markers.add(marker)
        }
    }


    private fun handleMapGestures(){
        val mapSettings = map?.uiSettings
        mapSettings?.isZoomGesturesEnabled = true
        mapSettings?.isScrollGesturesEnabled = true
    }

    override fun onLocationChanged(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude);
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 13f);
        map?.animateCamera(cameraUpdate);

    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        Toast.makeText(this, "klinketniego na marker", Toast.LENGTH_LONG).show()
        return false
    }

    private fun requestPermission(permissionType: String,
                                   requestCode: Int) {

        ActivityCompat.requestPermissions(this,
            arrayOf(permissionType), requestCode
        )
    }

    private fun getCurrentLocation(){
        if (map != null) {
            val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

            if (permission == PackageManager.PERMISSION_GRANTED) {
                map?.isMyLocationEnabled = true
                zoomToCurrentLocation()
                }

            } else {
                requestPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    LOCATION_REQUEST_CODE)
            }
        }


    private fun zoomToCurrentLocation(){
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {

        when (requestCode) {
            LOCATION_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] !=
                    PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,
                        R.string.location_denied_prompt,
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
