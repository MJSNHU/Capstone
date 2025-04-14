package com.CS360.project3;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EventList extends AppCompatActivity {
    private String TAG = "EventList";
    private ImageButton addBtn;
    private TextView username;
    private SharedPreferences mSharedPreferences;
    private ArrayList<Event> mEventList = null;
    private long uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mSharedPreferences = getSharedPreferences("shared_prefs", this.MODE_PRIVATE);
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        Log.d(TAG, "SMS Permission: " + ContextCompat.checkSelfPermission(EventList.this, android.Manifest.permission.SEND_SMS));

        Log.d(TAG, "phone: " + mSharedPreferences.getString("phone", ""));

        //TODO: save phone across user
        /*if ((ContextCompat.checkSelfPermission(EventList.this, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) && (mSharedPreferences.getString("phone", "").compareTo("") == 0)){
            Log.d(TAG, "Get phone number");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter phone number to send text to");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE);
            builder.setView(input);
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    edit.putString("phone", input.getText().toString());
                    edit.apply();
                    Log.d(TAG, "phone: " + mSharedPreferences.getString("phone", ""));



                }
            });
            builder.show();
        }
        */
        displayList();

    }

    private String convertDate(long epoch) {

        Date date = new Date(epoch);
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy HH:mm");
        String formattedDate = formatter.format(date);

        return formattedDate;
    }


    @Override
    public void onBackPressed(){


        super.onBackPressed();

        Intent i = new Intent(EventList.this, EventList.class);
        startActivity(i);

    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayList();
    }

    public void displayList(){
        ImageButton logoutBtn = findViewById(R.id.logout);
        addBtn = findViewById(R.id.add);
        username = findViewById(R.id.UserName);

        mSharedPreferences = getSharedPreferences("shared_prefs", this.MODE_PRIVATE);
        uid = mSharedPreferences.getLong("u_id", -1);
        Log.d(TAG, "userId: " + uid);
        username.setText(DBHelper.getUsername(uid));
        mEventList = DBHelper.getEvents(uid);

        ArrayList<EventsView> arrayList = new ArrayList<EventsView>();
        Log.d(TAG, "EventList size: " + mEventList.size());
        for (Event e: mEventList) {
            boolean alarm = false;
            if (e.alarm == 1) alarm = true;
            String date = convertDate(e.date);

            arrayList.add(new EventsView(e.e_id, date, e.event, alarm));
        }

        EventsViewAdapter eventArrayAdapter = new EventsViewAdapter(this, arrayList);

        ListView listView = findViewById(R.id.EventList);
        listView.setAdapter(eventArrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.d(TAG, "eventViewID: " + position + " " + id);
                Intent i = new Intent(EventList.this, EditEvent.class);
                Event e = mEventList.get((int) position);
                Log.d(TAG, "eventId: " + e.e_id);

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putLong("e_id", e.e_id);
                editor.putLong("e_uid", e.u_id);
                editor.putLong("date", e.date);
                editor.putString("event", e.event);
                editor.putString("details", e.details);
                if (e.alarm == 0) {
                    editor.putBoolean("alarm", false);
                } else {
                    editor.putBoolean("alarm", true);
                }
                editor.commit();
                Log.d(TAG, "get e_id: " + mSharedPreferences.getLong("e_id", -1));
                startActivity(i);

            }
        });


        logoutBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(),"Logged Out",Toast.LENGTH_LONG).show();

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putLong("u_id", -1);
                editor.apply();
                Log.d(TAG, "u_id = " + mSharedPreferences.getLong("u_id", -1));
                Intent i = new Intent(EventList.this, MainActivity.class);
                startActivity(i);
            }
        });


        addBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventList.this, EditEvent.class);
                Log.d(TAG, "Going to Edit Event");
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putLong("e_id", -1);
                editor.apply();
                startActivity(intent);
                Log.d(TAG, "Returned from EditEvent");
                finish();
            }
        });
    }

}