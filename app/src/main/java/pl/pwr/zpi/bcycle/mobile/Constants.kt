package pl.pwr.zpi.bcycle.mobile

const val API_BASE_URL = ""
const val M_TO_KM: Double = 1000.0
const val LOCATION_UPDATE_INTERVAL_MS: Long = 5000
// notifications
const val NOTIFICATION_CHANNEL_ONGOING_ID = "ongoing"
const val ONGOING_NOTIFICATION_ID = 1

// intent names
const val INTENT_LOCATION_UPDATE = "pl.pwr.zpi.bcycle.mobile.LOCATION_UPDATE"
const val INTENT_START_TRIP = "pl.pwr.zpi.bcycle.mobile.START_TRIP"

// startActivityForResult request codes
const val REQUEST_CODE_AFTER_GOOGLE_PLAY = 101

const val PERMISSIONS_REQUEST_LOCAITON = 102