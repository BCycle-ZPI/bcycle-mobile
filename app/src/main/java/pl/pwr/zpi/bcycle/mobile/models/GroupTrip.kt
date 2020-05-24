package pl.pwr.zpi.bcycle.mobile.models

import org.threeten.bp.ZonedDateTime

class GroupTrip(
    val id: Int?,
    val name: String,
    val description: String,
    val host: UserInfo?,
    val tripCode: String?,
    val startDate: ZonedDateTime,
    val endDate: ZonedDateTime,
    val route: List<GroupTripPoint>,
    val participants: List<GroupTripParticipant>?,
    var sharingUrl: String?,
    val photos: List<String>
)
    : TripTemplate() {
    constructor(
        name: String,
        description: String,
        startDate: ZonedDateTime,
        endDate: ZonedDateTime,
        route: List<GroupTripPoint>
    ) : this(null, name, description, null, null, startDate, endDate, route, null, null, listOf())


    override val sortKey: ZonedDateTime
        get() = startDate
}