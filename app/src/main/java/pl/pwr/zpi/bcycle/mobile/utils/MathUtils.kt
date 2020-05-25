package pl.pwr.zpi.bcycle.mobile.utils

import android.content.Context
import android.util.TypedValue

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}
fun Int.dpToPx(context: Context): Int {
    val metrics = context.resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), metrics).toInt()
}

fun Float.dpToPx(context: Context): Float {
    val metrics = context.resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, metrics)
}
