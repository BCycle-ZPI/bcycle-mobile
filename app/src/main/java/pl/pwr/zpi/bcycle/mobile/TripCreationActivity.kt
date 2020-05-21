package pl.pwr.zpi.bcycle.mobile

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_trip_creation.*
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import pl.pwr.zpi.bcycle.mobile.utils.content
import pl.pwr.zpi.bcycle.mobile.utils.showToastWarning
import java.text.SimpleDateFormat
import java.util.*


class TripCreationActivity : AppCompatActivity() {

    companion object{
        val formatterDateTime = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).withZone(ZoneId.systemDefault())
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_creation)
        setListeners()
        supportActionBar?.title = getString(R.string.create_new_trip)
    }

    private fun setListeners() {
        nextBT.setOnClickListener {
            if(isFormCorrect()){
                startActivity(createIntentAndSaveData())
            }
        }
        startdateBT.setOnClickListener{chooseDateTime(starttimeTV, startdateTV, true)
        }
        finishdateBT.setOnClickListener{chooseDateTime(finishtimeTV, finishdateTV, false)
        }
    }

    private fun createIntentAndSaveData() : Intent{
        val intent = Intent(this, TripCreationMapActivity::class.java)
        val bundle = Bundle()
        intent.putExtra(NAME_KEY,  nameET.text.toString())

        bundle.putSerializable(START_DATE_KEY,ZonedDateTime.parse(startdateTV.text.toString() + " " +  starttimeTV.text.toString(), formatterDateTime))
        bundle.putSerializable(END_DATE_KEY,ZonedDateTime.parse( finishdateTV.text.toString() + " " +  finishtimeTV.text.toString(), formatterDateTime))
        intent.putExtra(DESCRIPTION_KEY,  descET.text.toString())
        intent.putExtras(bundle)
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
        val dateTimeStart = startdateTV.text.toString() + " " +  starttimeTV.text.toString()
        val dateTimeEnd = finishdateTV.text.toString() + " " +  finishtimeTV.text.toString()
        val parsedDateStart = ZonedDateTime.parse(dateTimeStart, formatterDateTime)
        val parsedDateEnd = ZonedDateTime.parse(dateTimeEnd,  formatterDateTime)

        if(parsedDateStart.isBefore(ZonedDateTime.now()) || parsedDateEnd.isBefore(ZonedDateTime.now())){
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
        if(nameET.text.toString().isEmpty()){
            nameET.error = resources.getString(R.string.prompt_fill_field)
        }
        if(descET.text.toString().isEmpty()){
            descET.error = resources.getString(R.string.prompt_fill_field)
        }
    }

    private fun checkIfFieldsFilled() : Boolean {
        val nameStr = nameET.content()
        val descStr = descET.content()
        return nameStr.isNotEmpty() && descStr.isNotEmpty()
    }

    private fun checkIfDatesChosen():Boolean{
        val startDateStr = startdateTV.content()
        val startTimeStr = starttimeTV.content()
        val endDateStr = finishdateTV.content()
        val endTimeStr = finishtimeTV.content()
        return startDateStr.isNotEmpty() && startTimeStr.isNotEmpty() && endDateStr.isNotEmpty() && endTimeStr.isNotEmpty()
    }
}
