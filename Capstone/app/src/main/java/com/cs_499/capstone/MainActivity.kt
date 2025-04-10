package com.cs_499.capstone



import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity() {
    lateinit var db: DBHelper.EventDatabase
    lateinit var eDao: DBHelper.AllEventDao
    lateinit var uDao: DBHelper.UserDao
    lateinit var uEDao: DBHelper.UserEventDao
    private val coroutineContext: CoroutineContext = newSingleThreadContext("loadDB")
    private val scope = CoroutineScope(coroutineContext)
    private val TAG = "MainActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }

        // Check for events table; create if they don't exist
        val events = getEventsFromCSV(applicationContext)
        Log.d(TAG, events.size.toString())
        db = DBHelper.EventDatabase.getDatabaseInstance(this)
        eDao = db.allEventDao()
        uDao = db.userDao()
        uEDao = db.userEventDao()
        scope.launch() {
            Log.d(TAG, eDao.hasEventTable().toString())
            if (eDao.hasEventTable() == false) {
                eDao.insertAll(events)
            }
        }
    }

    // change intent upon clicking sign-in button
    fun signIn(view: View) {
        val intent = Intent(this, SignIn::class.java)
        startActivity(intent)
    }

}