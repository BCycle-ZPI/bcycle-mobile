package pl.pwr.zpi.bcycle.mobile.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import org.threeten.bp.ZonedDateTime
import pl.pwr.zpi.bcycle.mobile.INTENT_LOCATION_UPDATE
import pl.pwr.zpi.bcycle.mobile.INTENT_START_TRIP
import pl.pwr.zpi.bcycle.mobile.models.*

class TripLocationTrackingService : Service() {
    private val myBinder = LocalBinder()
    private var tripState: OngoingTripState = OngoingTripState.NOT_STARTED
    private lateinit var locationProvider: FusedLocationProviderClient
    private lateinit var ongoingTrip: OngoingTrip
    private var pendingIntent: PendingIntent? = null

    override fun onBind(intent: Intent): IBinder? {
        return myBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == INTENT_LOCATION_UPDATE) {
            handleLocationUpdate(intent)
        } else if (intent?.action == INTENT_START_TRIP) {
            locationProvider = LocationServices.getFusedLocationProviderClient(this)
            ongoingTrip = OngoingTrip()
            startListeningForLocation()
        }
        return START_NOT_STICKY
    }

    private fun startListeningForLocation() {
        if (pendingIntent != null) throw IllegalStateException(
            "Already listening for location"
        )
        pendingIntent = PendingIntent.getService(
            this,
            0,
            Intent(INTENT_LOCATION_UPDATE),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        tripState = OngoingTripState.ONGOING
    }

    private fun stopListeningForLocation() {
        if (pendingIntent == null) return
        locationProvider.removeLocationUpdates(pendingIntent)
        pendingIntent = null
    }

    private fun handleLocationUpdate(intent: Intent) {
        if (!LocationResult.hasResult(intent)) return
        val result = LocationResult.extractResult(intent)
        result.locations.forEach {
            ongoingTrip.events.add(OngoingTripEvent.midpoint(it))
        }
    }

    fun pauseTrip() {
        if (tripState == OngoingTripState.NOT_STARTED || tripState == OngoingTripState.FINISHED) {
            throw IllegalStateException("Cannot pause, trip is finished or not started")
        }
        stopListeningForLocation()
        ongoingTrip.events.add(OngoingTripEvent.pause())
        tripState = OngoingTripState.PAUSED
    }

    fun unpauseTrip() {
        if (tripState == OngoingTripState.ONGOING) {
            throw IllegalStateException("Cannot unpause, trip is ongoing")
        }
        ongoingTrip.events.add(OngoingTripEvent.unpause())
        startListeningForLocation()
    }

    fun endTrip() {
        stopListeningForLocation()
        tripState = OngoingTripState.FINISHED
        if (ongoingTrip.events.isNotEmpty()) {
            val lastEvent = ongoingTrip.events.last()
            if (lastEvent.eventType == OngoingTripEventType.PAUSE) {
                // The trip effectively ended at the pause event, so we can set the endTime to that and delete the pause event.
                ongoingTrip.finished = lastEvent.timeReached
                ongoingTrip.events.removeAt(ongoingTrip.events.lastIndex)
            }
        }
        if (ongoingTrip.finished == null) {
            ongoingTrip.finished = ZonedDateTime.now()
        }
    }

    fun getTripPoints(): List<TripPoint> {
        return ongoingTrip.getTripPoints()
    }

    fun getTrip(): Trip {
        if (tripState != OngoingTripState.FINISHED) {
            throw IllegalStateException("Can only get finished trip, call endTrip first")
        }
        return ongoingTrip.asTrip()
    }

    inner class LocalBinder : Binder() {
        fun getService(): TripLocationTrackingService {
            return this@TripLocationTrackingService
        }
    }
}
