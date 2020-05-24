package pl.pwr.zpi.bcycle.mobile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.TooltipCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.content_record_trip.*
import okhttp3.MediaType
import okhttp3.RequestBody
import pl.pwr.zpi.bcycle.mobile.api.ApiClient
import pl.pwr.zpi.bcycle.mobile.models.OngoingTripEvent
import pl.pwr.zpi.bcycle.mobile.services.TripLocationTrackingService
import pl.pwr.zpi.bcycle.mobile.utils.background
import pl.pwr.zpi.bcycle.mobile.utils.timeToString
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream


class RecordTripActivity : BCycleNavigationDrawerActivity(), OnMapReadyCallback {
    private lateinit var service: TripLocationTrackingService
    private var isBound: Boolean = false
    private var canStart: Boolean = false
    private var time: Double = 0.0
    private var lastClockUpdate: Long = -1
    private val timerHandler = Handler()
    private var madeImages: MutableList<Uri> = mutableListOf()
    private var uploadedImageCount = 0
    private val madeAnyImages: Boolean
        get() = madeImages.isNotEmpty()

    private lateinit var mapFragment: SupportMapFragment
    private var map: GoogleMap? = null
    private var polyline: Polyline? = null
    private var polylineList: MutableList<LatLng> = mutableListOf()
    private var anyPointsShown = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.i(LOG_TAG, "Location recording service connected.")
            synchronized(this) {
                // We've bound to TripLocationTrackingService, cast the IBinder and get TripLocationTrackingService instance
                val binder = service as TripLocationTrackingService.LocalBinder
                this@RecordTripActivity.service = binder.getService()
                isBound = true
                if (canStart) {
                    Log.i(LOG_TAG, "Starting trip (via canStart).")
                    startOrContinueTrip()
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    private val timerRunnable = object : Runnable {
        override fun run() {
            val currentTime = System.currentTimeMillis()
            if (lastClockUpdate == -1L) lastClockUpdate = currentTime
            time += (currentTime - lastClockUpdate) / MS_TO_S
            lastClockUpdate = currentTime
            timerHandler.postDelayed(this, 1000)
            updateTimeDisplay(time)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_trip)
        TooltipCompat.setTooltipText(photoBt, getString(R.string.take_a_photo))

        getMaps()
        configureIndependentNavigationDrawer()
        updateNavigationDrawerHeader()

        pauseFAB.setOnClickListener {
            if (isBound) {
                service.togglePauseTrip()
            }
            if (service.isPaused) {
                stopTimer()
                pauseFAB.setImageDrawable(getDrawable(R.drawable.ic_play_white))
            } else {
                startTimer()
                pauseFAB.setImageDrawable(getDrawable(R.drawable.ic_pause_white))
            }
        }

        photoBt.setOnClickListener {
            makePhotoWithPermissions()
        }

        stopFAB.setOnClickListener {
            if (isBound) {
                stopTimer()
                uploadTripData()
            }
        }
    }

    private fun makePhotoWithPermissions() {
        ensurePermissions(
            this::makePhoto,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            R.string.camera_storage_permission_title,
            R.string.camera_storage_permission_message,
            PERMISSIONS_REQUEST_CAMERA_STORAGE
        )
    }

    private fun makePhoto() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "camera_photo")
        values.put(MediaStore.Images.Media.DESCRIPTION, "camera photo during trip")
        mPhotoUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val takenPictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takenPictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri)

        if (takenPictureIntent.resolveActivity(this.packageManager) != null)
            startActivityForResult(takenPictureIntent, MAKE_IMAGE_REQUEST)
        else
            Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MAKE_IMAGE_REQUEST && mPhotoUri != null) {
            if (resultCode == Activity.RESULT_OK) {
                madeImages.add(mPhotoUri!!)
                mPhotoUri = null
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    @SuppressLint("CheckResult")
    private fun uploadTripData() {
        stopFAB.isEnabled = false
        pauseFAB.isEnabled = false
        photoBt.isEnabled = false
        uploadingCard.visibility = View.VISIBLE
        if (madeAnyImages) {
            uploadingPB.visibility = View.VISIBLE
            uploadingPB.max = madeImages.size + 1
        } else {
            uploadingPB.visibility = View.GONE
        }
        service.endTrip()
        val trip = service.getTrip()
        ApiClient.tripApi.post(trip)
            .background().subscribe(
                { result ->
                    if (madeAnyImages) {
                        savePhotos(result.result)
                        // also calls handleTripUploadSuccess
                    } else {
                        handleTripUploadSuccess(result.result)
                    }
                }, { error ->
                    showTripUploadError(error.message)
                }
            )
    }

    @SuppressLint("CheckResult")
    private fun savePhotos(tripId: Int) {
        if (madeAnyImages) {
            uploadingPB.progress = 1
            uploadingText.text = resources.getQuantityString(
                R.plurals.uploading_photos, madeImages.size, madeImages.size
            )
            for (photo in madeImages) {
                val bytePhoto = readBytes(photo)
                if (bytePhoto == null) {
                    Log.e(LOG_TAG, "Failed to read photo $photo, skipping")
                    handlePhotoUploadSuccess(tripId, null)
                } else {
                    val imageBody = RequestBody.create(
                        MediaType.parse("image/jpeg"), bytePhoto
                    )
                    ApiClient.tripApi.putPhoto(tripId, imageBody).background().subscribe(
                        { result -> handlePhotoUploadSuccess(tripId, result.result) },
                        { error -> showTripUploadError(error.message, true) }
                    )
                }
            }
        }
    }

    private fun readBytes(uri: Uri): ByteArray? {
        val stream: InputStream?
        try {
            stream = contentResolver.openInputStream(uri)
        } catch (e: FileNotFoundException) {
            return null
        }
        if (stream == null) return null
        val byteArrayStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var i = Int.MAX_VALUE
        while (stream.read(buffer, 0, buffer.size).also { i = it } > 0) {
            byteArrayStream.write(buffer, 0, i)
        }

        return byteArrayStream.toByteArray()
    }

    private fun handleTripUploadSuccess(tripId: Int) {
        // TODO go to the trip page
        service.stopSelf()
        finish()
    }

    private fun handlePhotoUploadSuccess(tripId: Int, photoUrl: String?) {
        if (photoUrl == null) {
            Log.e(LOG_TAG, "A photo was skipped")
        } else {
            Log.d(LOG_TAG, "Photo upload URL: $photoUrl")
        }
        uploadedImageCount += 1
        uploadingPB.progress = uploadedImageCount + 1
        if (uploadedImageCount == madeImages.size) {
            handleTripUploadSuccess(tripId)
        }
    }

    private fun showTripUploadError(description: String?, isPhotosError: Boolean = false) {
        stopFAB.isEnabled = true
        pauseFAB.isEnabled = true
        photoBt.isEnabled = true
        uploadingCard.visibility = View.GONE
        val descriptionAuto = description ?: getString(R.string.uploading_error_generic)

        val builder = AlertDialog.Builder(this)
        builder.setMessage(descriptionAuto)
            .setTitle(if (isPhotosError) R.string.uploading_error_title_photos else R.string.uploading_error_title)
            .setPositiveButton(
                R.string.upload_try_again
            ) { _, _ -> uploadTripData() }

        val dialog = builder.create()
        dialog.show()
    }

    private fun newLocation(location: OngoingTripEvent) {
        val latLng = location.asLatLng()
        polylineList.add(latLng)
        if (map != null) {
            animateCameraToPoint(latLng)
        }
        polyline?.points = polylineList
    }

    private fun animateCameraToPoint(latLng: LatLng) {
        if (anyPointsShown) {
            map!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        } else {
            map!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            anyPointsShown = true
        }
    }

    private fun updateDistance(distance: Double) {
        distanceTV.text = getString(R.string.distance_format, distance)
    }

    private fun updateTimeDisplay(time: Double) {
        timeTV.text = timeToString(time)
    }

    private fun updateTimeFromService(time: Double) {
        stopTimer()
        this.time = time
        lastClockUpdate = System.currentTimeMillis()
        startTimer()
    }

    private fun startTimer() {
        timerHandler.postDelayed(timerRunnable, 1000)
    }

    private fun stopTimer() {
        timerHandler.removeCallbacks(timerRunnable)
        lastClockUpdate = -1
    }

    private fun startOrContinueTripWithWait() {
        synchronized(this) {
            canStart = true
            if (!isBound) {
                Log.i(LOG_TAG, "Allowing to start trip after binding")
            } else {
                Log.i(LOG_TAG, "Starting trip (after binding)")
                startOrContinueTrip()
            }
        }
    }

    private fun startOrContinueTrip() {
        service.startOrContinueTrip(
            this::updateDistance,
            this::updateTimeFromService,
            this::newLocation,
            polylineList::clear
        )
        // IsMyLocationEnabled requires the permission.
        // We try to do this in two cases due to both permissions and maps being asynchronous.
        map?.isMyLocationEnabled = true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_LOCAITON -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startOrContinueTripWithWait()
                } else {
                    showPermissionExplanationDialog(
                        this::finish,
                        R.string.location_permission_title,
                        R.string.location_permission_message
                    )
                }
                return
            }

            PERMISSIONS_REQUEST_CAMERA_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    makePhoto()
                } else {
                    showPermissionExplanationDialog(
                        { },
                        R.string.camera_storage_permission_title,
                        R.string.camera_storage_permission_message
                    )
                }
                return
            }

            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun ensureLocationPermissionAndStartTrip() {
        ensurePermissions(
            this::startOrContinueTripWithWait,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            R.string.location_permission_title,
            R.string.location_permission_message,
            PERMISSIONS_REQUEST_LOCAITON
        )
    }

    private fun getMaps() {
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.myMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        synchronized (this) {
            if (isBound && canStart) map?.isMyLocationEnabled = true
        }
        val mapSettings = map!!.uiSettings
        mapSettings.isZoomGesturesEnabled = true
        mapSettings.isScrollGesturesEnabled = true
        mapSettings.isMyLocationButtonEnabled = true
        mapSettings.isMapToolbarEnabled = true
        polyline = map!!.addPolyline(PolylineOptions().width(MAP_POLYLINE_WIDTH).addAll(polylineList))
        if (polylineList.isNotEmpty()) {
            animateCameraToPoint(polylineList.last())
        }
        // https://stackoverflow.com/a/49247322
        val mapView = mapFragment.view ?: return
        val locationButton =
            (mapView.findViewById<View>(Integer.parseInt("1")).parent as View).findViewById<View>(
                Integer.parseInt("2")
            )
        val rlp = locationButton.layoutParams as (RelativeLayout.LayoutParams)
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        rlp.setMargins(0, 0, 30, 30)
    }

    override fun onStart() {
        super.onStart()

        // Bind to TripLocationTrackingService
        val intent = Intent(this, TripLocationTrackingService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        ensureLocationPermissionAndStartTrip()
    }

    override fun onStop() {
        super.onStop()
        service.stopCallbacks()
        unbindService(connection)
        isBound = false
    }

    companion object {
        private val LOG_TAG = "BCycle-Rec"
        private var mPhotoUri: Uri? = null
    }
}
