package pl.pwr.zpi.bcycle.mobile

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.TooltipCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.content_record_trip.*
import pl.pwr.zpi.bcycle.mobile.api.ApiClient
import pl.pwr.zpi.bcycle.mobile.models.OngoingTripEvent
import pl.pwr.zpi.bcycle.mobile.services.TripLocationTrackingService
import pl.pwr.zpi.bcycle.mobile.utils.background
import pl.pwr.zpi.bcycle.mobile.utils.timeToString

import java.io.ByteArrayOutputStream


class RecordTripActivity : BCycleNavigationDrawerActivity() {
    private lateinit var service: TripLocationTrackingService
    private var isBound: Boolean = false
    private var canStart: Boolean = false
    private var time: Double = 0.0
    private var lastClockUpdate: Long = -1
    private val timerHandler = Handler()
    private var madeImages: List<Uri> =  listOf<Uri>()

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
            makePhoto()
        }

        stopFAB.setOnClickListener {
            if (isBound) {
                stopTimer()
                uploadTripData()
            }
        }
    }

    private fun makePhoto() {

        val values  = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "camera_photo")
        values.put(MediaStore.Images.Media.DESCRIPTION, "camera photo during trip")
        mPhotoUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)

        val takenPictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takenPictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri)




        if(takenPictureIntent.resolveActivity(this.packageManager)!=null)
            startActivityForResult(takenPictureIntent,MAKE_IMAGE_REQUEST )
        else
            Toast.makeText(this,"Unable to open camera",Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
        if(requestCode == MAKE_IMAGE_REQUEST && mPhotoUri !=null) {
            madeImages+= listOf(mPhotoUri!!)

            mPhotoUri = null
        }
        else
            super.onActivityResult(requestCode, resultCode, data)

    private fun uploadTripData() {
        stopFAB.isEnabled = false
        pauseFAB.isEnabled = false
        photoBt.isEnabled = false
        uploadingCard.visibility = View.VISIBLE
        service.endTrip()
        val trip = service.getTrip()
        val sub = ApiClient.tripApi.post(trip)
            .background().subscribe(
                { result ->
                    savePhotos(result.result)
                    handleTripUploadSuccess(result.result) },
                { error ->
                    showTripUploadError(error.message)
                }
            )
    }

    private  fun savePhotos(tripId: Int) {

        val emptyList  = emptyList<Uri>()

        if(madeImages !=emptyList) {
            for  (photo in madeImages){
                val bytePhoto = readBytes(photo)

                bytePhoto?.let { ApiClient.tripApi.putPhoto(tripId, it).background().subscribe() }
            }
        }
    }

    private fun readBytes(uri:Uri): ByteArray? {
        val stream = contentResolver.openInputStream(uri)
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

    private fun showTripUploadError(description: String?) {
        stopFAB.isEnabled = true
        pauseFAB.isEnabled = true
        photoBt.isEnabled = true
        uploadingCard.visibility = View.GONE
        val descriptionAuto = description ?: getString(R.string.uploading_error_generic)

        val builder = AlertDialog.Builder(this)
        builder.setMessage(descriptionAuto)
            .setTitle(R.string.uploading_error_title)
            .setPositiveButton(
                R.string.upload_try_again
            ) { _, _ -> uploadTripData() }

        val dialog = builder.create()
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun newLocation(location: OngoingTripEvent) {
        // TODO placeholder
        val newText = "\n(${location.latitude}, ${location.longitude})"
        mapPlaceholder.text = mapPlaceholder.text.toString() + newText
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
            if (!isBound) {
                Log.i(LOG_TAG, "Allowing to start trip after binding")
                canStart = true
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
            this::newLocation
        )
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
                    showPermissionExplanationDialog(this::finish)
                }
                return
            }

            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun showPermissionExplanationDialog(andThen: () -> Unit) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val dialog = builder
            .setMessage(R.string.location_permission_message)
            .setTitle(R.string.location_permission_title)
            .setPositiveButton(android.R.string.ok) { _, _ -> andThen() }
            .create()
        dialog.show()
    }


    private fun ensureLocationPermissionAndStartTrip() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                showPermissionExplanationDialog(this::ensureLocationPermissionAndStartTrip)
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_LOCAITON
                )
            }
        } else {
            startOrContinueTripWithWait()
        }
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
