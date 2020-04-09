package pl.pwr.zpi.bcycle.mobile.utils

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.chrono.IsoChronology
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatterBuilder

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