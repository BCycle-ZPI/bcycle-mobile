package pl.pwr.zpi.bcycle.mobile.models

import org.threeten.bp.ZonedDateTime

class Trip(
    val id: Int?,
    val distance: Double,
    val time: Int,
    val started: ZonedDateTime,
    val finished: ZonedDateTime,
    val groupTripId: Int?,
    val route: List<TripPoint>,
    val photos: List<String>
) : TripTemplate() {
    constructor(
        distance: Double,
        time: Int,
        started: ZonedDateTime,
        finished: ZonedDateTime,
        route: List<TripPoint>
    ) :
            this(null, distance, time, started, finished, null, route, listOf())
}
