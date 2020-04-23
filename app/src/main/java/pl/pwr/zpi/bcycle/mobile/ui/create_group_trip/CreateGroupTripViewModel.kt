package pl.pwr.zpi.bcycle.mobile.ui.create_group_trip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreateGroupTripViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is create_group_trip Fragment"
    }
    val text: LiveData<String> = _text
}