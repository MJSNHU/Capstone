package com.CS360.project3;


import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class EditEvent extends AppCompatActivity {
    String TAG = "EditEvent";

    private Button timeButton, dateButton;
    private int hour, min;
    private EditText event, details;
    private SwitchCompat toggle;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private DBHelper db;
    private SharedPreferences mSharedPreferences;
    private long mUid;
    private Event e = new Event();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_event);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Log.d(TAG, "onCreate");
        //e.e_id = -1;
        this.setTheme(R.style.FireBrick_Theme);
        timeButton = findViewById(R.id.time);
        dateButton = findViewById(R.id.date);
        event = findViewById(R.id.event);
        details = findViewById(R.id.details);
        toggle = findViewById(R.id.toggle);
        if( ContextCompat.checkSelfPermission(EditEvent.this, android.Manifest.permission.SEND_SMS) !=0 ) {
            toggle.setChecked(false);
            toggle.setEnabled(false);

        } else {
            toggle.setEnabled(true);
        }


        mSharedPreferences = getSharedPreferences("shared_prefs", this.MODE_PRIVATE);

        Log.d(TAG, String.valueOf(mSharedPreferences.getLong("e_id",-1)));
        e.e_id = mSharedPreferences.getLong("e_id",-1);
        Log.d(TAG,"e_id: " + e.e_id);
        if (e.e_id > -1) {
            e.u_id = mSharedPreferences.getLong("u_id", -1);
            e.date = mSharedPreferences.getLong("date", -1);
            e.event = mSharedPreferences.getString("event", "");
            e.details = mSharedPreferences.getString("details", "");
            Log.d(TAG, "Event: " + e.e_id + " " + e.u_id + " " + e.date + " " + e.event + " " + e.details);
            if (mSharedPreferences.getBoolean("alarm", false)) {
                e.alarm = 1;
            } else {
                e.alarm = 0;
            }
        } else {
            e.u_id = -1;
            e.date = -1;
            e.event = "";
            e.details = "";
            e.alarm = 0;
        }

        initDatePicker();
        initTimePicker();

        if (e.date >= 0) {
            timeButton.setText(getFormattedTime(e.date));
            dateButton.setText(getFormattedDate(e.date));
        } else {
            dateButton.setText(getTodaysDate());
            Calendar now = Calendar.getInstance();
            timeButton.setText((now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE)));
        }

        event.setText(e.event);
        details.setText(e.details);
        if (e.alarm == 0) {
            toggle.setChecked(false);
        } else {
            toggle.setChecked(true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(EditEvent.this, EventList.class));

    }

    private void initTimePicker() {
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String time = hourOfDay + ":" + minute;
                timeButton.setText(time);
            }
        };

        int hourOfDay, minute;
        Calendar cal;
        Log.d(TAG, "e.date: " + e.date);
        if (e.e_id > -1) {
            cal = Calendar.getInstance();
            cal.setTimeInMillis(e.date);
        } else {
            cal = Calendar.getInstance();
        }

        hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        minute = cal.get(Calendar.MINUTE);

        Log.d(TAG, e.date + " " + hourOfDay + ":" + minute);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        timePickerDialog = new TimePickerDialog(this, style, timeSetListener, hourOfDay, minute, true);

    }

    private String getTodaysDate()
    {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) +1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day, month, year);
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                month = month + 1;
                String date = makeDateString(dayOfMonth, month, year);
                dateButton.setText(date);
            }
        };

        int year, month, day;
        Calendar cal;
        Log.d(TAG, "e.date: " + e.date);
        if (e.e_id > -1) {
            cal = Calendar.getInstance();
            cal.setTimeInMillis(e.date);
        } else {
            cal = Calendar.getInstance();
        }

            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            day = cal.get(Calendar.DAY_OF_MONTH);
            Log.d(TAG, e.date + " " + year + " " + month + " " + day);
        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);

    }

    private String makeDateString(int day, int month, int year) {
        return (getMonthFormat(month) + " " + day + " " + year);
    }

    private String getMonthFormat ( int month){
        if (month == 1)
            return "Jan";
        if (month == 2)
            return "Feb";
        if (month == 3)
            return "Mar";
        if (month == 4)
            return "Apr";
        if (month == 5)
            return "May";
        if (month == 6)
            return "Jun";
        if (month == 7)
            return "Jul";
        if (month == 8)
            return "Aug";
        if (month == 9)
            return "Sep";
        if (month == 10)
            return "Oct";
        if (month == 11)
            return "Nov";
        if (month == 12)
            return "Dec";
        return "Err";
    }

    public void popDatePicker(View view) {

        datePickerDialog.show();
    }


    public void popTimePicker(View view) {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {


            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMin) {

                hour = selectedHour;
                min = selectedMin;
                DecimalFormat formatter = new DecimalFormat("00");
                String hourFormatted = formatter.format(hour);
                String minuteFormatted = formatter.format(min);
                timeButton.setText(hourFormatted + ":" + minuteFormatted);
            }
        };

        int style = AlertDialog.THEME_HOLO_LIGHT;


        TimePickerDialog timePickerDialog = new TimePickerDialog(this, style, onTimeSetListener, hour, min, true);
        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }
    private String getFormattedTime(long epoch)
    {
        Date date = new Date(epoch);
        SimpleDateFormat formatter = new SimpleDateFormat ("HH:mm");
        String formattedTime = formatter.format(date);
        return formattedTime;
    }

    private String getFormattedDate(long epoch)
    {
        Date date = new Date(epoch);
        SimpleDateFormat formatter = new SimpleDateFormat ("MMM dd yyyy");
        String formattedTime = formatter.format(date);
        return formattedTime;
    }

    private String makeTimeString(int hour, int min) {
        return (hour + ":" + min);
    }

    public void saveEvent(View view) {

        Button dateBtn = findViewById(R.id.date);
        Button timeBtn = findViewById(R.id.time);
        EditText eventEdit = findViewById(R.id.event);
        EditText detailsEdit = findViewById(R.id.details);
        SwitchCompat toggle = findViewById(R.id.toggle);
        String time = timeBtn.getText().toString();
        String date = dateBtn.getText().toString();
        String eventName = eventEdit.getText().toString();
        String details = detailsEdit.getText().toString();

        Event event = new Event();
        event.u_id = getSharedPreferences("shared_prefs", MODE_PRIVATE).getLong("u_id", -1);
        event.details = details;
        event.event = eventName;
        event.e_id = e.e_id;

        event.date = getEpochTime(date, time);
        if (toggle.isChecked()) {
            event.alarm = 1;
        } else {
            event.alarm = 0;
        }

        Log.d(TAG, "Save event e_id: "+ event.e_id);
        if (event.e_id == -1) {

            db = DBHelper.getInstance(this);
            Log.d(TAG, event.e_id + " " + event.u_id + " " + event.date + " " + event.event + " " + event.details + " " + event.alarm);
            long id = db.addEvent(event);
            Log.d(TAG, "add event result: " + id);
            if (id > 0) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putLong("e_id", id);
                editor.apply();
                Intent intent = new Intent(EditEvent.this, EventList.class);
                startActivity(intent);
            } else {
                Log.d(TAG, "save Event Failed");
            }
        } else {
            db = DBHelper.getInstance(this);
            long id = db.updateEvent(event);
            Log.d(TAG, "update result: " + id);
            set_removeAlarm(event);
        }
        finish();
    }

    private void set_removeAlarm(Event event) {
        Log.d(TAG, "set_removeAlarm");
        Intent intent = new Intent(EditEvent.this, AlarmReceiver.class);
        intent.putExtra("message", event.event);
        intent.putExtra("e_id", event.e_id);
        Log.d(TAG, "Event: " + event.event + " " + intent.getStringExtra("message"));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) event.e_id, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (event.alarm == 1) {
            Log.d(TAG, "Event.date: " + event.date);
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,e.date,pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
        }
    }

    public void deleteEvent(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(EditEvent.this);
        builder.setMessage("Are you sure you want to delete this event?");
        builder.setTitle("Delete Event");
        builder.setCancelable(false);
        builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
            // When the user click yes button then app will close
            return;
        });

        // Set the Negative button with No name Lambda OnClickListener method is use of DialogInterface interface.
        builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
            // If user click no then dialog box is canceled.
            dialog.cancel();
            db = DBHelper.getInstance(this);
            Log.d(TAG, "event id: " + e.e_id);
            long result = db.removeEvent(e.e_id);


            if (result <= 0) {
                Log.d(TAG, "Delete failed");
                //TODO:
                //Toast toast = new Toast(this);
                //toast.makeText(getBaseContext(), "Could not delete event", Toast.LENGTH_LONG);
                //toast.show();
            } else if (result == 1) {
                Log.d(TAG, "Event Deleted");
                //TODO:
                //Toast toast = new Toast(getBaseContext());
                //toast.makeText(getBaseContext(), "Event deleted", Toast.LENGTH_LONG);
                //toast.show();
            } else {
                Log.d(TAG, "deleted more than one event");
            }

            Log.d(TAG, "result: " + result);
            Intent i = new Intent(EditEvent.this, EventList.class);
            startActivity(i);


        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();


    }

    public long getEpochTime(String date, String time) {
        Log.d(TAG, date + " " + time);
        long epoch = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm");

        String dateTime = date + " " + time;
        Log.d(TAG, dateTime);
        try {
            Date d = dateFormat.parse(dateTime);
            epoch = d.getTime();

        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        Log.d(TAG, "epoch: " + epoch);

   return epoch;


    }

}