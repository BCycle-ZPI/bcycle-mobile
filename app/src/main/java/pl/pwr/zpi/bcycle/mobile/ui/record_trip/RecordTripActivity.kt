package pl.pwr.zpi.bcycle.mobile.ui.record_trip

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.TooltipCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.content_record_trip.*
import pl.pwr.zpi.bcycle.mobile.PERMISSIONS_REQUEST_LOCAITON
import pl.pwr.zpi.bcycle.mobile.R
import pl.pwr.zpi.bcycle.mobile.models.OngoingTripEvent
import pl.pwr.zpi.bcycle.mobile.services.TripLocationTrackingService

class RecordTripActivity : AppCompatActivity() {
    private lateinit var service: TripLocationTrackingService
    private var isBound: Boolean = false
    private var canStart: Boolean = true

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            synchronized(this) {
                // We've bound to TripLocationTrackingService, cast the IBinder and get TripLocationTrackingService instance
                val binder = service as TripLocationTrackingService.LocalBinder
                val service = binder.getService()
                this@RecordTripActivity.service = service
                isBound = true
                if (canStart) startOrContinueTrip()
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_trip)
        TooltipCompat.setTooltipText(photoBt, getString(R.string.take_a_photo))

        pauseFAB.setOnClickListener {
            if (isBound) {
                service.togglePauseTrip()
            }
            pauseFAB.setImageDrawable(getDrawable(if (service.isPaused) R.drawable.ic_play_white else R.drawable.ic_pause_white))
        }

        stopFAB.setOnClickListener {
            if (isBound) {
                service.endTrip()
                // TODO send to API and stop service
            }
        }
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


    private fun startOrContinueTripWithWait() {
        synchronized(this) {
            if (!isBound) {
                canStart = true
            } else {
                startOrContinueTrip()
            }
        }
    }
    private fun startOrContinueTrip() {
        service.startOrContinueTrip(this::updateDistance, this::newLocation)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_LOCAITON -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startOrContinueTripWithWait()
                } else {
                    showPermissionExplanationDialog(this::finish)
                    finish()
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
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
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

}
