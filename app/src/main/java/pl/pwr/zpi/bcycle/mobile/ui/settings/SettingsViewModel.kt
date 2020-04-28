package pl.pwr.zpi.bcycle.mobile.ui.settings

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.*
import com.google.firebase.storage.FirebaseStorage
import pl.pwr.zpi.bcycle.mobile.LoginActivity
import pl.pwr.zpi.bcycle.mobile.MainActivity
import pl.pwr.zpi.bcycle.mobile.R
import pl.pwr.zpi.bcycle.mobile.utils.showToast

class SettingsViewModel: ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is settings Fragment"
    }
    val text: LiveData<String> = _text



}