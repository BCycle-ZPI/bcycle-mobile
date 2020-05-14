package pl.pwr.zpi.bcycle.mobile

const val API_BASE_URL = "https://bcycle.azurewebsites.net/api/"
const val HTTP_TIMEOUT_S: Long = 20
const val M_TO_KM: Double = 1000.0
const val MS_TO_S: Double = 1000.0
const val LOCATION_UPDATE_INTERVAL_MS: Long = 5000
// notifications
const val NOTIFICATION_CHANNEL_ONGOING_ID = "ongoing"
const val ONGOING_NOTIFICATION_ID = 1

// intent names and extras
const val INTENT_LOCATION_UPDATE = "pl.pwr.zpi.bcycle.mobile.LOCATION_UPDATE"
const val INTENT_START_TRIP = "pl.pwr.zpi.bcycle.mobile.START_TRIP"
const val INTENT_EXTRA_MAIN_NAV_ID = "navID"

// startActivityForResult request codes
const val REQUEST_CODE_AFTER_GOOGLE_PLAY = 101

const val PERMISSIONS_REQUEST_LOCAITON = 102


const  val PICK_IMAGE_REQUEST = 1
const  val MAKE_IMAGE_REQUEST = 2