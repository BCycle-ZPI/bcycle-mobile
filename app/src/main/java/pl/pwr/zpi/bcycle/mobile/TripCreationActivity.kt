package pl.pwr.zpi.bcycle.mobile

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_trip_creation.*
import net.danlew.android.joda.JodaTimeAndroid
import org.joda.time.format.DateTimeFormat
import pl.pwr.zpi.bcycle.mobile.utils.setMargins
import pl.pwr.zpi.bcycle.mobile.utils.showToast
import pl.pwr.zpi.bcycle.mobile.utils.showToastWarning
import java.text.SimpleDateFormat
import java.util.*


class TripCreationActivity : AppCompatActivity() {

    companion object{
        val TIME_FORMAT = "HH:mm"
        val DATE_FORMAT = "dd-MM-yyyy"
        val DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm"
        val START_DATE_KEY = "START_DATE_KEY"
        val START_TIME_KEY = "START_TIME_KEY"
        val END_DATE_KEY = "END_DATE_KEY"
        val END_TIME_KEY = "END_TIME_KEY"
        val NAME_KEY = "NAME_KEY"
        val DESCRIPTION_KEY = "DESCRIPTION_KEY"

    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_creation)
        setListeners()
        supportActionBar?.hide()
        JodaTimeAndroid.init(this)
    }
    
    private fun setListeners() {
        bt_next.setOnClickListener {
            if(isFormCorrect()){
                startActivity(createIntentAndSaveData())
            }
        }
        bt_start_date.setOnClickListener{chooseDateTime(tv_start_time, tv_start_date, true)
        }
        bt_finish_date.setOnClickListener{chooseDateTime(tv_finish_time, tv_finish_date, false)
        }
    }

    private fun createIntentAndSaveData() : Intent{
        val intent = Intent(this, TripCreationMapActivity::class.java)
        intent.putExtra(NAME_KEY,  et_name.text.toString())
        intent.putExtra(START_DATE_KEY, tv_start_date.text.toString())
        intent.putExtra(START_TIME_KEY, tv_start_time.text.toString())
        intent.putExtra(END_DATE_KEY, tv_finish_date.text.toString())
        intent.putExtra(END_TIME_KEY, tv_finish_time.text.toString())
        intent.putExtra(DESCRIPTION_KEY,  et_desc.text.toString())
        return intent
    }

    private fun chooseDateTime(timeTV:TextView, dateTV:TextView, startDate:Boolean) {
        val cal = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            timeTV.text = SimpleDateFormat(TIME_FORMAT).format(cal.time)
        }
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, day)
            dateTV.text = SimpleDateFormat(DATE_FORMAT).format(cal.time)
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

        if(startDate){
            bt_start_date.setMargins(rightMarginDp = 80)
        }
        else{
            bt_finish_date.setMargins(rightMarginDp = 80)
        }
    }

    private fun isFormCorrect(): Boolean {
        if(!checkIfFieldsFilled()){
            setErrors()
            return false
        }
        if(!checkIfDatesChosen()){
            showToastWarning(R.string.prompt_choose_dates)
            return false
        }
        if(!checkIfDatesValid()){
            return false
        }
        return true
    }

    private fun checkIfDatesValid():Boolean{
        val dateTimeStart = tv_start_date.text.toString() + " " +  tv_start_time.text.toString()
        val dateTimeEnd = tv_finish_date.text.toString() + " " +  tv_finish_time.text.toString()
        val formatterDateTime = DateTimeFormat.forPattern(DATE_TIME_FORMAT)
        val parsedDateStart = formatterDateTime.parseDateTime(dateTimeStart)
        val parsedDateEnd = formatterDateTime.parseDateTime(dateTimeEnd)

        if(parsedDateStart.isBeforeNow || parsedDateEnd.isBeforeNow){
            showToastWarning(R.string.prompt_chosen_dates_from_past)
            return false
        }

        if(parsedDateStart.isAfter(parsedDateEnd)){
            showToastWarning(R.string.prompt_invalid_dates)
            return false
        }
        return true
    }

    private fun setErrors(){
        if(et_name.text.toString().isEmpty()){
            et_name.error = resources.getString(R.string.prompt_fill_field)
        }
        if(et_desc.text.toString().isEmpty()){
            et_desc.error = resources.getString(R.string.prompt_fill_field)
        }
    }

    private fun checkIfFieldsFilled() : Boolean {
        val nameStr = et_name.text.toString()
        val descStr = et_desc.text.toString()
        return nameStr.isNotEmpty() && descStr.isNotEmpty()
    }

    private fun checkIfDatesChosen():Boolean{
        val startDateStr = tv_start_date.text.toString()
        val startTimeStr = tv_start_time.text.toString()
        val endDateStr = tv_finish_date.text.toString()
        val endTimeStr = tv_finish_time.text.toString()
        return startDateStr.isNotEmpty() && startTimeStr.isNotEmpty() && endDateStr.isNotEmpty() && endTimeStr.isNotEmpty()
    }
}
