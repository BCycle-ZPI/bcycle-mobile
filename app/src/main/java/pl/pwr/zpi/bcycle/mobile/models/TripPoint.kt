package pl.pwr.zpi.bcycle.mobile.models

import org.threeten.bp.ZonedDateTime

data class TripPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val timeReached: ZonedDateTime
) :TripPointTemplate(latitude,longitude)
