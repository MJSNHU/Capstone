package com.cs_499.capstone

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Search : AppCompatActivity() {

    private val TAG = "Search"
    private lateinit var startDatePickerDialog: DatePickerDialog
    private lateinit var endDatePickerDialog: DatePickerDialog
    private lateinit var startDateBtn: Button
    private lateinit var endDateBtn: Button
    private var mEndEpoch: Long = 0
    private var mStartEpoch: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        startDateBtn = findViewById(R.id.dateStart)
        endDateBtn =  findViewById(R.id.dateEnd)
        startDatePickerDialog = DatePickerDialog(this)
        endDatePickerDialog = DatePickerDialog(this)

        initDateStartPicker()
        initDateEndPicker()

    }

    private fun initDateStartPicker() {
        val dateSetListener =
            OnDateSetListener { view, year, month, dayOfMonth ->
                var month = month
                month = month + 1
                startDateBtn.text  = makeDateString(dayOfMonth, month, year)
                mStartEpoch = epochFromString(year.toString() + "-" + month.toString().padStart(2,'0') +"-" + dayOfMonth.toString().padStart(2,'0') + "T00:00+")

            }

        val year: Int
        val month: Int
        val day: Int

        // default start date
        year = 2024
        month = 6
        day = 24
        val style = AlertDialog.THEME_HOLO_LIGHT

        startDatePickerDialog = DatePickerDialog(this, style, dateSetListener, year, month, day)

    }

    private fun initDateEndPicker() {
        val dateSetListener =
            OnDateSetListener { view, year, month, dayOfMonth ->
                var month = month
                month = month + 1
                var date = makeDateString(dayOfMonth, month, year)
                //2024-08-02T20:37:00+02:00
                mEndEpoch = epochFromString(year.toString() + "-" + month.toString().padStart(2,'0') +"-" + dayOfMonth.toString().padStart(2,'0') + "T23:59+")
                endDateBtn.text = date

            }

        val year: Int
        val month: Int
        val day: Int

        // default end date
        year = 2024
        month = 7
        day = 12
        val style = AlertDialog.THEME_HOLO_LIGHT

        endDatePickerDialog = DatePickerDialog(this, style, dateSetListener, year, month, day)

    }

    fun popDateStartPicker(view: View) {
        startDatePickerDialog.show()
    }

    fun popDateEndPicker(View: View) {
        endDatePickerDialog.show()
    }

    private fun makeDateString(day: Int, month: Int, year: Int): String {
        return (month.toString().padStart(2,'0') + "/" + day.toString().padStart(2,'0') + "/" + year.toString())
    }

    // gets keyword, start and end date, sets them as the result
    fun search(view: View) {

        // ensure end date is after start date
        if (mEndEpoch < mStartEpoch) {
            val toast = Toast.makeText(this@Search, "End date must be greater than start date", Toast.LENGTH_LONG)
            toast.show()
        } else {

            var keyword: String = findViewById<EditText>(R.id.keyword).text.toString()
            var startDate: String = findViewById<Button>(R.id.dateStart).text.toString()
            var endDate: String = findViewById<Button>(R.id.dateEnd).text.toString()
            var start: Long
            var end: Long
            if (keyword == "" ) {
                keyword = "*"
            }
            if (startDate == "Start Date") {
                start = epochFromString("2024-07-11T00:00:00+")
            } else {
                var year: String = startDate.split("/")[2]
                var day: String = startDate.split("/")[1]
                var month: String = startDate.split("/")[0]
                startDate = year + "-" + month + "-" + day + "T" + "00:00:00+"
                start = epochFromString(startDate)
            }
            if (endDate == "End Date") {
                end = epochFromString("2024-08-11T23:59:59+")
            } else {
                var year: String = endDate.split("/")[2]
                var day: String = endDate.split("/")[1]
                var month: String = endDate.split("/")[0]
                endDate = year + "-" + month + "-" + day + "T" + "23:59:59+"
                end = epochFromString(endDate)

            }

            var term = "%"+keyword+"%"
            intent.putExtra("searchTerm", term)
            intent.putExtra("start", start)
            intent.putExtra("end", end)
            Log.d(TAG, intent.getStringExtra("searchTerm").toString())
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

    }
}