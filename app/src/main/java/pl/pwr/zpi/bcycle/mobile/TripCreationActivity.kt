package pl.pwr.zpi.bcycle.mobile

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_trip_creation.*
import java.text.SimpleDateFormat
import java.util.*


class TripCreationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_creation)
        setListeners()
    }

    private fun setListeners() {
        bt_next.setOnClickListener {
            if (isFormFilled()) {
                //startActivity()
            } else {
                Toast.makeText(this, R.string.form_not_filled_info, Toast.LENGTH_LONG).show()
            }
        }
        bt_start_date.setOnClickListener{chooseDateTime(tv_start_time, tv_start_date)}
        bt_finish_date.setOnClickListener{chooseDateTime(tv_finish_time, tv_finish_date)}
        bt_next.setOnClickListener {
            startActivity(Intent(this, TripCreationMapActivity::class.java))
        }
    }


    private fun chooseDateTime(timeTV:TextView, dateTV:TextView) {

        val cal = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            timeTV.text = SimpleDateFormat("HH:mm").format(cal.time)
        }
        val dateSetListener = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, day)
            dateTV.text = SimpleDateFormat("dd-MM-yyyy").format(cal.time)
        }
        TimePickerDialog(
            this,
            timeSetListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
        DatePickerDialog(
            this,
            dateSetListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun isFormFilled(): Boolean {
        return true //todo
    }

}
