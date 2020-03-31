package pl.pwr.zpi.bcycle.mobile

import org.gavaghan.geodesy.Ellipsoid
import org.gavaghan.geodesy.GeodeticCalculator
import org.gavaghan.geodesy.GlobalPosition
import org.threeten.bp.Duration
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

/** Given a list of TripPoints, get the time between them in seconds. */
fun getTime(tripPoints: List<TripPoint>): Int {
    val d = Duration.between(tripPoints.first().timeReached, tripPoints.last().timeReached)
    return d.seconds.toInt()
}
