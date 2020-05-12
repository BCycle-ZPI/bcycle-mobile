package pl.pwr.zpi.bcycle.mobile.utils

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import es.dmoral.toasty.Toasty

fun EditText.content() = text.toString()

fun AppCompatActivity.showToast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun AppCompatActivity.showToast(stringId: Int) =
    Toast.makeText(this, resources.getText(stringId), Toast.LENGTH_SHORT).show()

fun AppCompatActivity.showToastError(stringId: Int) =
    Toasty.error(this, resources.getText(stringId), Toast.LENGTH_SHORT, true).show();

fun AppCompatActivity.showToastWarning(stringId: Int) =
    Toasty.warning(this, resources.getText(stringId), Toast.LENGTH_SHORT, true).show();

//extensions methods
fun View.setMargins(
    leftMarginDp: Int? = null,
    topMarginDp: Int? = null,
    rightMarginDp: Int? = null,
    bottomMarginDp: Int? = null
) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        val params = layoutParams as ViewGroup.MarginLayoutParams
        leftMarginDp?.run { params.leftMargin = this.dpToPx(context) }
        topMarginDp?.run { params.topMargin = this.dpToPx(context) }
        rightMarginDp?.run { params.rightMargin = this.dpToPx(context) }
        bottomMarginDp?.run { params.bottomMargin = this.dpToPx(context) }
        requestLayout()
    }
}



