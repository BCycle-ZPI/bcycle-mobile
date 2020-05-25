package pl.pwr.zpi.bcycle.mobile

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener
import com.google.android.gms.maps.model.Polyline
import pl.pwr.zpi.bcycle.mobile.utils.dpToPx

class PolylineResizeListener(private val map: GoogleMap, private val polyline: Polyline, private val context: Context) : OnCameraMoveListener {
    init {
        onCameraMove()
    }

    override fun onCameraMove() {
        val currentZoom = map.cameraPosition.zoom
        val widthDip =
            (MAP_POLYLINE_WIDTH_MIN +
                (currentZoom - MAP_POLYLINE_WIDTH_MIN_AT) * MAP_POLYLINE_WIDTH_GROWTH)
            .coerceIn(MAP_POLYLINE_WIDTH_MIN, MAP_POLYLINE_WIDTH_MAX)
        polyline.width = widthDip.dpToPx(context)
    }
}