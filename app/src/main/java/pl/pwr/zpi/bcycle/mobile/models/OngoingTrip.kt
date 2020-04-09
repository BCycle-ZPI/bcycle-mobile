package pl.pwr.zpi.bcycle.mobile.models

import org.threeten.bp.ZonedDateTime
import pl.pwr.zpi.bcycle.mobile.utils.getDistance
import pl.pwr.zpi.bcycle.mobile.utils.getTime


data class OngoingTrip(
    val started: ZonedDateTime,
    var finished: ZonedDateTime?,
    val events: MutableList<OngoingTripEvent>
) {
    fun getTripPoints(): List<TripPoint> {
        return events.filter { e -> e.eventType == OngoingTripEventType.MIDPOINT }
            .map { e -> e.asTripPoint() }
    }

    fun asTrip(): Trip {
        if (finished == null) {
            throw IllegalStateException("Only finished trips can be converted")
        }
        val allPoints = mutableListOf<TripPoint>()
        var distance = 0.0
        var time = 0
        // Calculate distance and time based on pauses and unpauses, considering the groups separate.
        // Time spent and distance travelled during a pause is ignored.
        val currentGroup = mutableListOf<TripPoint>()
        var isPaused = false
        for (e in events) {
            // Ignore all actions when paused.
            if (isPaused && e.eventType == OngoingTripEventType.UNPAUSE) {
                isPaused = false
                continue
            } else if (isPaused) continue

            if (e.eventType == OngoingTripEventType.PAUSE) {
                // A group ends when the trip is paused.
                distance += getDistance(
                    currentGroup
                )
                time += getTime(currentGroup)
                allPoints.addAll(currentGroup)
                currentGroup.clear()
            } else if (e.eventType == OngoingTripEventType.MIDPOINT) {
                currentGroup.add(e.asTripPoint())
            }
        }
        // Take care of the last group.
        distance += getDistance(currentGroup)
        time += getTime(currentGroup)
        allPoints.addAll(currentGroup)

        return Trip(distance, time, started, finished!!, allPoints)
    }

    constructor() : this(ZonedDateTime.now(), null, mutableListOf())
}
