package pl.pwr.zpi.bcycle.mobile.models

import org.threeten.bp.ZonedDateTime

data class TripPoint(
    val id: Int?,
    val tripId: Int?,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val timeReached: ZonedDateTime
) {
    constructor(
        latitude: Double,
        longitude: Double,
        altitude: Double?,
        timeReached: ZonedDateTime
    ) :
            this(null, null, latitude, longitude, altitude, timeReached)
}
