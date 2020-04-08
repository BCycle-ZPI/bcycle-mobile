package pl.pwr.zpi.bcycle.mobile.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import org.threeten.bp.ZonedDateTime
import pl.pwr.zpi.bcycle.mobile.*
import pl.pwr.zpi.bcycle.mobile.models.*
import pl.pwr.zpi.bcycle.mobile.RecordTripActivity

class TripLocationTrackingService : Service() {
    private val myBinder = LocalBinder()
    private var tripState: OngoingTripState = OngoingTripState.NOT_STARTED
    private lateinit var locationProvider: FusedLocationProviderClient
    private lateinit var ongoingTrip: OngoingTrip
    private var hasOngoingTrip = false
    private lateinit var notificationManager: NotificationManager
    private var distanceUpdateCallback: ((Double) -> Unit)? = null
    private var newLocationCallback: ((OngoingTripEvent) -> Unit)? = null
    private var pendingIntent: PendingIntent? = null
    private var currentDistance = 0.0

    val isPaused: Boolean
        get() = tripState == OngoingTripState.PAUSED

    override fun onBind(intent: Intent): IBinder? {
        return myBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == INTENT_LOCATION_UPDATE) {
            handleLocationUpdate(intent)
        } else if (intent?.action == INTENT_START_TRIP) {
            startNewTrip()
        }
        return START_NOT_STICKY
    }

    fun startOrContinueTrip(
        distanceUpdateCallback: ((Double) -> Unit)?,
        newLocationCallback: ((OngoingTripEvent) -> Unit)?
    ) {
        if (hasOngoingTrip) {
            if (pendingIntent == null) startListeningForLocation()
        } else {
            startNewTrip()
        }
        this.distanceUpdateCallback = distanceUpdateCallback
        this.newLocationCallback = newLocationCallback

        // restore data in activity
        distanceUpdateCallback?.invoke(currentDistance)
        if (newLocationCallback != null) {
            ongoingTrip.events.filter { it.isMidPoint }
                .forEach(newLocationCallback)
        }
    }

    fun stopCallbacks() {
        this.distanceUpdateCallback = null
        this.newLocationCallback = null
    }

    fun startNewTrip() {
        locationProvider = LocationServices.getFusedLocationProviderClient(this)
        ongoingTrip = OngoingTrip()
        hasOngoingTrip = true
        currentDistance = 0.0
        startListeningForLocation()
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
        val locationRequest = LocationRequest()
        locationRequest.interval = LOCATION_UPDATE_INTERVAL_MS
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationProvider.requestLocationUpdates(locationRequest, pendingIntent)
        createNotificationAndStartForeground()
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
        var lastLocation = ongoingTrip.events.lastOrNull { it.isMidPoint }
        result.locations.forEach {
            val newPoint = OngoingTripEvent.midpoint(it)
            ongoingTrip.events.add(newPoint)
            if (lastLocation != null) {
                currentDistance += getDistance(lastLocation!!, newPoint)
            }
            newLocationCallback?.invoke(newPoint)
            lastLocation = newPoint
        }
        distanceUpdateCallback?.invoke(currentDistance)
    }

    fun togglePauseTrip() {
        if (tripState == OngoingTripState.PAUSED) unpauseTrip() else pauseTrip()
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val id = NOTIFICATION_CHANNEL_ONGOING_ID
        // The user-visible name of the channel.
        val name = getString(R.string.notification_channel_ongoing_name)
        // The user-visible description of the channel.
        val description = getString(R.string.notification_channel_ongoing_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(id, name, importance)
        // Configure the notification channel.
        channel.description = description
        channel.setShowBadge(false)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotificationAndStartForeground() {
        notificationManager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val notificationBuilder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ONGOING_ID)

        val openIntent = Intent(applicationContext, RecordTripActivity::class.java)
        openIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        openIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        // TODO more informative notification
        notificationBuilder
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setContentIntent(openPendingIntent)
            .setContentTitle(getString(R.string.notification_ongoing_title))
            .setContentIntent(openPendingIntent)
            .setSmallIcon(R.drawable.bike_icon)
        startForeground(
            ONGOING_NOTIFICATION_ID, notificationBuilder.build())
    }

    inner class LocalBinder : Binder() {
        fun getService(): TripLocationTrackingService {
            return this@TripLocationTrackingService
        }
    }

}
