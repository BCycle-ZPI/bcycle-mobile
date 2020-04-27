package pl.pwr.zpi.bcycle.mobile.ui.join_group_trip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class JoinGroupTripViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is join_group_trip Fragment"
    }
    val text: LiveData<String> = _text
}