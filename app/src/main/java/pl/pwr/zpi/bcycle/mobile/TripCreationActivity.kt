package pl.pwr.zpi.bcycle.mobile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_trip_creation.*

class TripCreationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_creation)
        setListeners()
    }

    private fun setListeners() {
        bt_next.setOnClickListener{
            if(isFormFilled()){
                //startActivity()
            }
            else {
                Toast.makeText(this, R.string.form_not_filled_info, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isFormFilled(): Boolean {

    }
}
