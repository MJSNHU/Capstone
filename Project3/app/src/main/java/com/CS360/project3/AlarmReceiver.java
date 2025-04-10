package com.CS360.project3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import android.os.Bundle;

public class AlarmReceiver extends BroadcastReceiver {

    String TAG = "AlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {

        long e_id = intent.getLongExtra("e_id", -1);
        String message = intent.getStringExtra("message");
        if (message.compareTo("") == 0) {
            message = "Empty Event Title";
        }
        Log.d(TAG, e_id + " " + message);
        //Toast.makeText(context, "Event " + e_id + " is triggered", Toast.LENGTH_LONG).show();
        SendMessage sms = new SendMessage();
        sms.sendSMS("15551234567", message);
    }
}
