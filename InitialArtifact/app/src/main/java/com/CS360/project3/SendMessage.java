package com.CS360.project3;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.AlarmManager;
import android.content.Context;
import android.telephony.SmsManager;
import android.widget.TimePicker;

public class SendMessage {


    public void sendSMS(String phone, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phone, "", message, null, null);
    }

}