package pl.pwr.zpi.bcycle.mobile

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.gavaghan.geodesy.Ellipsoid
import org.gavaghan.geodesy.GeodeticCalculator
import org.gavaghan.geodesy.GlobalPosition
import org.threeten.bp.Duration
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.chrono.IsoChronology
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatterBuilder
import org.threeten.bp.format.ResolverStyle
import pl.pwr.zpi.bcycle.mobile.models.OngoingTripEvent
import pl.pwr.zpi.bcycle.mobile.models.OngoingTripEventType
import pl.pwr.zpi.bcycle.mobile.models.TripPoint

/** Given a list of TripPoints, get the distance between them in km. */
fun getDistance(tripPoints: List<TripPoint>): Double {
    return tripPoints.zipWithNext { a, b -> getDistance(a, b) }.sum()
}

/** Given two TripPoints, get the distance between them in km. */
fun getDistance(from: TripPoint, to: TripPoint): Double {
    // via https://stackoverflow.com/a/8468979
    val geoCalc = GeodeticCalculator()
    val reference = Ellipsoid.WGS84
    val pos1 = GlobalPosition(from.latitude, from.longitude, from.altitude ?: 0.0)
    val pos2 = GlobalPosition(to.latitude, to.longitude, to.altitude ?: 0.0)
    return geoCalc.calculateGeodeticCurve(reference, pos2, pos1).ellipsoidalDistance / M_TO_KM
}


/** Given two OngoingTripEvents, get the distance between them in km. */
fun getDistance(from: OngoingTripEvent, to: OngoingTripEvent): Double {
    if (from.eventType != OngoingTripEventType.MIDPOINT || to.eventType != OngoingTripEventType.MIDPOINT) {
        throw IllegalArgumentException("Distance can be calculated only between midpoints.")
    }
    // via https://stackoverflow.com/a/8468979
    val geoCalc = GeodeticCalculator()
    val reference = Ellipsoid.WGS84
    val pos1 = GlobalPosition(from.latitude!!, from.longitude!!, from.altitude ?: 0.0)
    val pos2 = GlobalPosition(to.latitude!!, to.longitude!!, to.altitude ?: 0.0)
    return geoCalc.calculateGeodeticCurve(reference, pos2, pos1).ellipsoidalDistance / M_TO_KM
}

/** Given a list of TripPoints, get the time between them in seconds. */
fun getTime(tripPoints: List<TripPoint>): Int {
    val d = Duration.between(tripPoints.first().timeReached, tripPoints.last().timeReached)
    return d.seconds.toInt()
}

fun <T> Single<T>.background(): Single<T>
    = this
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())


private var isoUtcFormatter =
        DateTimeFormatterBuilder().parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME).appendOffset("+HH:MM:ss", "Z")
            .toFormatter()
            .withChronology(IsoChronology.INSTANCE)

fun dateToIso(date: ZonedDateTime): String =
    date.withZoneSameInstant(ZoneId.of("UTC")).format(isoUtcFormatter)

fun dateFromIso(date: String): ZonedDateTime =
    ZonedDateTime.parse(date, isoUtcFormatter)

fun localDateFromIso(date: String): ZonedDateTime =
    ZonedDateTime.parse(date, isoUtcFormatter).withZoneSameInstant(ZoneId.systemDefault())
