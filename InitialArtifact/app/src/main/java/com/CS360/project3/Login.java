package com.CS360.project3;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Login extends AppCompatActivity {

    private EditText username, password, verify_password;
    private Button login, register;
    private User user = new User();
    private String TAG = "Login";
    private SharedPreferences mSharedPreferences;
    private String verified_password = null;
    private boolean registerEnabled;
    private int SEND_SMS_REQUEST_CODE = 1001;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mSharedPreferences = getSharedPreferences("shared_prefs", this.MODE_PRIVATE);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        verify_password = findViewById(R.id.verify_password);
        registerEnabled = true;

        Log.d(TAG, "Checking Permissions");
        if (ContextCompat.checkSelfPermission(
                Login.this, android.Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Request permission");
            explainPermission();

        }

        username.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (validateFields()) {
                    user.username = s.toString();
                }
            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (validateFields()) {
                    user.password = s.toString();
                }
            }
        });

        verify_password.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (validateFields()) {
                    verified_password = s.toString();
                }
            }
        });
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
        Log.d(TAG, "onResume");

    }

    private boolean validateFields() {

        if (username.getText().length() > 0 && password.getText().length() > 0) {
            login.setEnabled(true);
            register.setEnabled(true);
            login.setBackgroundColor(ContextCompat.getColor(this, R.color.FireBrick));
            register.setBackgroundColor(ContextCompat.getColor(this, R.color.FireBrick));

            return true;
        } else {
            login.setEnabled(false);
            register.setEnabled(false);
            login.setBackgroundColor(ContextCompat.getColor(this, R.color.DarkGray));
            register.setBackgroundColor(ContextCompat.getColor(this, R.color.DarkGray));

            return false;
        }

    }

    public void register(View view) {

        Log.d(TAG, "registerClicked");
        Log.d(TAG, "Verified password" + verified_password);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);

        this.user.username = username.getText().toString();
        this.user.password = password.getText().toString();

        long id = DBHelper.getInstance(this).retrieveUser(user);
        Log.d(TAG, "retrieveUser: " + id);

        if (id > 0) {
            Toast toast = Toast.makeText(this, "Username taken", Toast.LENGTH_LONG);
            toast.show();
        } else {
            verify_password = findViewById(R.id.verify_password);
            if (verify_password.getVisibility() == View.GONE) {
                verify_password.setVisibility(View.VISIBLE);
                registerEnabled = true;
            }
        }

        if (registerEnabled) {


            this.verified_password = verify_password.getText().toString();
            if (this.verified_password.length() > 0) {
                if (verified_password.compareTo(this.password.getText().toString()) == 0 && username.getText().toString().length() > 0 && password.getText().toString().length() > 0) {
                    Log.d(TAG, "password verification match");

                    id = DBHelper.getInstance(this).addUser(user);

                    if (id > 0) {
                        //ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS);

                        Log.d(TAG, "id: " + id);
                        Log.d(TAG, "user inserted");
                        Toast toast = Toast.makeText(Login.this, "User registered", Toast.LENGTH_LONG);
                        toast.show();
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putLong("userId", id);
                        editor.apply();

                        verify_password.setVisibility(View.GONE);
                    } else {
                        Log.d(TAG, "user not inserted");
                    }

                } else {
                    Toast toast = Toast.makeText(Login.this, "Passwords do not match", Toast.LENGTH_LONG);
                    toast.show();
                    Log.d(TAG, "verified_password does not match");
                }

            }

        }
    }

    public void signIn (View view) {
        Log.d(TAG, "signInClicked");
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        long uid;
        this.user.username = username.getText().toString();
        this.user.password = password.getText().toString();
        Intent i;

        uid = DBHelper.verifyUser(user);

        Log.d(TAG, "uid: " + uid);

        if (uid > 0) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putLong("u_id", uid);
            editor.apply();
            i = new Intent(Login.this, MainActivity.class);


        } else {
            Log.d(TAG, "userId:  " + uid);
            Toast toast = Toast.makeText(this, "Invalid credentials", Toast.LENGTH_LONG);
            toast.show();
            i = new Intent(this, Login.class);

        }
        startActivity(i);
        finish();

    }

    private void explainPermission() {

        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
        builder.setTitle("Permissions Requested");
        builder.setMessage("This app can send SMS messages to remind you of an event but requires your permission.");
        builder.setCancelable(false);

        builder.setNegativeButton("Deny", (DialogInterface.OnClickListener) (dialog, which) -> {
        });

        builder.setPositiveButton("Grant", (DialogInterface.OnClickListener) (dialog, which) -> {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_REQUEST_CODE);
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Log.d(TAG, "showAlert");


    }
}