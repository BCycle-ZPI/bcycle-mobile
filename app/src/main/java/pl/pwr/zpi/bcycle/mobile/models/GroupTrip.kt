package pl.pwr.zpi.bcycle.mobile.models

import org.threeten.bp.ZonedDateTime

class GroupTrip(
    val id: Int?,
    val name: String,
    val description: String,
    val host: UserInfo,
    val tripCode: String?,
    val startDate: ZonedDateTime,
    val endDate: ZonedDateTime,
    val route: List<GroupTripPoint>,
    val participants: List<GroupTripParticipant>?
)
    : TripTemplate()