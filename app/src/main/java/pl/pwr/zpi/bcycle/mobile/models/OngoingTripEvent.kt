package pl.pwr.zpi.bcycle.mobile.models

import android.location.Location
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

data class OngoingTripEvent(
    val latitude: Double?,
    val longitude: Double?,
    val altitude: Double?,
    val timeReached: ZonedDateTime,
    val eventType: OngoingTripEventType
) {

    fun asTripPoint(): TripPoint {
        if (eventType != OngoingTripEventType.MIDPOINT) {
            throw UnsupportedOperationException("Only midpoints can be converted to TripPoints")
        }
        return TripPoint(latitude!!, longitude!!, altitude, timeReached)
    }

    companion object {
        fun pause(): OngoingTripEvent {
            return OngoingTripEvent(
                null, null, null, ZonedDateTime.now(), OngoingTripEventType.PAUSE
            )
        }

        fun unpause(): OngoingTripEvent {
            return OngoingTripEvent(
                null, null, null, ZonedDateTime.now(), OngoingTripEventType.UNPAUSE
            )
        }

        fun midpoint(location: Location): OngoingTripEvent {
            return midpoint(location.latitude, location.longitude, location.altitude, location.time)
        }

        fun midpoint(
            latitude: Double,
            longitude: Double,
            altitude: Double?,
            epochMilli: Long
        ): OngoingTripEvent {
            val instant = Instant.ofEpochMilli(epochMilli)
            val dateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
            return OngoingTripEvent(
                latitude, longitude, altitude, dateTime, OngoingTripEventType.MIDPOINT
            )
        }
    }
}
