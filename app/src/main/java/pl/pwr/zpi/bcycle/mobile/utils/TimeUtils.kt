package pl.pwr.zpi.bcycle.mobile.utils

import org.threeten.bp.Duration
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.chrono.IsoChronology
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatterBuilder
import pl.pwr.zpi.bcycle.mobile.MS_TO_S

private var isoUtcFormatter =
    DateTimeFormatterBuilder().parseCaseInsensitive()
        .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME).appendOffset("+HH:MM:ss", "Z")
        .toFormatter()
        .withChronology(IsoChronology.INSTANCE)

fun dateToIso(date: ZonedDateTime): String =
    date.withZoneSameInstant(ZoneId.of("UTC")).format(
        isoUtcFormatter
    )

fun dateFromIso(date: String): ZonedDateTime =
    ZonedDateTime.parse(
        date,
        isoUtcFormatter
    )

fun localDateFromIso(date: String): ZonedDateTime =
    ZonedDateTime.parse(
        date,
        isoUtcFormatter
    ).withZoneSameInstant(ZoneId.systemDefault())

/** Get time between two dates in seconds. */
fun getTime(from: ZonedDateTime, to: ZonedDateTime): Int {
    val d = Duration.between(from, to)
    return d.seconds.toInt()
}

/** Get time between two dates in seconds. */
fun getTimeExact(from: ZonedDateTime, to: ZonedDateTime): Double {
    val d = Duration.between(from, to)
    return d.toMillis() / MS_TO_S
}

fun timeToString(time: Double): String {
    val lTime = time.toLong()
    val seconds = lTime % 60
    val minutes = (lTime / 60) % 60
    val hours = lTime / 3600
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}