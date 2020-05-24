package pl.pwr.zpi.bcycle.mobile.models

import org.threeten.bp.ZonedDateTime

abstract class TripTemplate {
    abstract val sortKey: ZonedDateTime
}