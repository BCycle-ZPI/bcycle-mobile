package pl.pwr.zpi.bcycle.mobile.ui.create_group_trip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import pl.pwr.zpi.bcycle.mobile.R

class CreateGroupTripFragment : Fragment() {

    private lateinit var createGroupTripViewModel: CreateGroupTripViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        createGroupTripViewModel = ViewModelProvider(this).get(CreateGroupTripViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_create_group_trip, container, false)
        val textView: TextView = root.findViewById(R.id.text_create_group_trip)
        createGroupTripViewModel.text.observe(viewLifecycleOwner, Observer { textView.text = it })
        return root
    }
}