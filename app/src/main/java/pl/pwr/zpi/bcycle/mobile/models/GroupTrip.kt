package pl.pwr.zpi.bcycle.mobile.models

import android.content.Context
import org.threeten.bp.ZonedDateTime
import pl.pwr.zpi.bcycle.mobile.R
import pl.pwr.zpi.bcycle.mobile.UPCOMING_GROUP_TRIP_OFFSET_MINUTES

class GroupTrip(
    val id: Int?,
    val name: String,
    val description: String,
    val host: UserInfo?,
    val tripCode: String?,
    val startDate: ZonedDateTime,
    val endDate: ZonedDateTime,
    val route: List<GroupTripPoint>,
    var participants: List<GroupTripParticipant>?,
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

    /** Return if the trip is about to start/upcoming. Upcoming group trips are defined as: starting in less than 30 minutes or ended less than 30 minutes ago. */
    fun aboutToStart(): Boolean {
        val now = ZonedDateTime.now()
        return startDate.minusMinutes(UPCOMING_GROUP_TRIP_OFFSET_MINUTES) <= now
                && now <= endDate.plusMinutes(UPCOMING_GROUP_TRIP_OFFSET_MINUTES)
    }

    fun formatParticipantCount(context: Context): String {
        if (participants == null) return "0"
        val acceptedCount = participants!!.filter { it.status == ParticipantStatus.ACCEPTED }.count()
        val pendingCount = participants!!.filter { it.status == ParticipantStatus.PENDING }.count()
        return if (pendingCount > 0) {
            context.getString(R.string.participants_count_with_pending, acceptedCount, pendingCount)
        } else {
            context.getString(R.string.participants_count, acceptedCount)
        }
    }

    override val sortKey: ZonedDateTime
        get() = startDate
}