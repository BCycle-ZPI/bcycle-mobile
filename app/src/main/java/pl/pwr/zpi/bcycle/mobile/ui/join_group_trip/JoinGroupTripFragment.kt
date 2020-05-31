package pl.pwr.zpi.bcycle.mobile.ui.join_group_trip

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import pl.pwr.zpi.bcycle.mobile.BCycleNavigationDrawerActivity
import pl.pwr.zpi.bcycle.mobile.R
import pl.pwr.zpi.bcycle.mobile.api.ApiClient
import pl.pwr.zpi.bcycle.mobile.utils.background
import pl.pwr.zpi.bcycle.mobile.utils.content
import pl.pwr.zpi.bcycle.mobile.utils.showToast
import retrofit2.HttpException

class JoinGroupTripFragment : Fragment() {

    private lateinit var joinGroupTripViewModel: JoinGroupTripViewModel
    private lateinit var joinBt: Button
    private lateinit var tripCodeET: EditText
    private lateinit var joinSpinner: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_join_group_trip, container, false)
        joinBt = root.findViewById(R.id.joinBt)
        tripCodeET = root.findViewById(R.id.tripCodeET)
        joinSpinner = root.findViewById(R.id.joinSpinner)
        joinBt.setOnClickListener { joinTrip() }
        return root
    }

    @SuppressLint("CheckResult")
    fun joinTrip() {
        val code = tripCodeET.content().trim()
        if (code.length != 6) {
            tripCodeET.error = getString(R.string.error_code_length)
            return
        } else {
            tripCodeET.error = null
        }
        joinBt.isEnabled = false
        joinSpinner.visibility = View.VISIBLE

        ApiClient.groupTripApi.join(code).background().subscribe({
            joinBt.isEnabled = true
            joinSpinner.visibility = View.GONE
            showToast(R.string.join_request_successful)
            (activity as BCycleNavigationDrawerActivity).navigateUpToHome()
        }, { err ->
            joinBt.isEnabled = true
            joinSpinner.visibility = View.GONE
            var message = err.localizedMessage
            if (err is HttpException) {
                if (err.code() == 404) message = getString(R.string.join_group_trip_404)
            }
            val builder = AlertDialog.Builder(activity!!)
            builder.setMessage(message)
                .setTitle(R.string.cannot_join_trip)
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .show()
        })
    }
}