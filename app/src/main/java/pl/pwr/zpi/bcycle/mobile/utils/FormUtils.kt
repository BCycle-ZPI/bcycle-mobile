package pl.pwr.zpi.bcycle.mobile.utils

import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

fun EditText.content() = text.toString()

fun AppCompatActivity.showToast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()