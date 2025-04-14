package com.CS360.project3;

import static androidx.core.app.ActivityCompat.requestPermissions;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private SharedPreferences mSharedPreferences;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Log.d(TAG, "onCreate");
        mSharedPreferences = getSharedPreferences("shared_prefs", this.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("phone","");
        editor.apply();


        Log.d(TAG, "creating database");
        //Create the database if it doesn't exist
        try {
            long created = DBHelper.getInstance(this).createDatabase();
            if (created == -1) {
                Log.d(TAG,"Unknown SQL error, check DBHelper Log");
            } else if (created == 0) {
                Log.d(TAG, "One or more tables already exist");
            } else if (created == 1) {
                Log.d(TAG, "Database created");
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

        Intent intent;

        userId = mSharedPreferences.getLong("u_id", -1);
        Log.d(TAG, String.valueOf(userId));
        if (userId == - 1) {
            Log.d(TAG, "not loggedIn");
            intent = new Intent(MainActivity.this, Login.class);
            //startActivity(intent);

        } else {
            Log.d(TAG, "loggedIn");
            intent = new Intent(this, EventList.class);
        }

        startActivity(intent);
        //finish();
    }





 }
