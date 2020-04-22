package pl.pwr.zpi.bcycle.mobile.ui.join_group_trip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import pl.pwr.zpi.bcycle.mobile.R

class JoinGroupTripFragment : Fragment() {

    private lateinit var joinGroupTripViewModel: JoinGroupTripViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        joinGroupTripViewModel = ViewModelProvider(this).get(JoinGroupTripViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_join_group_trip, container, false)
        val textView: TextView = root.findViewById(R.id.text_join_group_trip)
        joinGroupTripViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}