package pl.pwr.zpi.bcycle.mobile.models

import org.threeten.bp.ZonedDateTime

class Trip(
    val id: Int?,
    val distance: Double,
    val time: Int,
    val started: ZonedDateTime,
    val finished: ZonedDateTime,
    val mapImageUrl: String?,
    val groupTripId: Int?,
    val tripPoints: List<TripPoint>,
    val tripPhotos: List<String>
) {
    constructor(
        distance: Double,
        time: Int,
        started: ZonedDateTime,
        finished: ZonedDateTime,
        tripPoints: List<TripPoint>
    ) :
            this(null, distance, time, started, finished, null, null, tripPoints, listOf())
}
