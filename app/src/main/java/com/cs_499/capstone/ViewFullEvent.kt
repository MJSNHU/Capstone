package com.cs_499.capstone

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlin.coroutines.CoroutineContext

// Show full event; if coming from user events view, displays a delete icon
// if coming from all events view, displays an add icon
class ViewFullEvent : AppCompatActivity() {
    private val coroutineContext: CoroutineContext = newSingleThreadContext("searchDB")
    private val scope = CoroutineScope(coroutineContext)
    private val TAG = "ViewFullEvent"
    private var mChoice = ""
    private var mId: Long = -1
    private var mUser: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_full_event)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var id = intent.getLongExtra("eventId", -1)
        mId = id
        val db = DBHelper.EventDatabase.getDatabaseInstance(this)
        var event: DBHelper.Event
        var choice = intent.getStringExtra("choice")
        if (choice != null) {
            mChoice = choice
        }
        mUser = intent.getStringExtra("username")
        scope.launch {
            event = db.allEventDao().getEvent(id)
            Thread(kotlinx.coroutines.Runnable {
                this@ViewFullEvent.runOnUiThread {
                    var timeText: TextView = findViewById(R.id.time)
                    var dateText: TextView = findViewById(R.id.date)
                    var detailText: TextView = findViewById(R.id.details)

                    dateText.text = stringFromEpoch(event.startDate).split(" ")[0]
                    timeText.text = stringFromEpoch(event.startDate).split(" ")[1]
                    detailText.text = event.discipline + "\n\n" + event.phase + "\n\n" + event.locationDescription.replace("\"","")
                    detailText.textSize = 36.0F

                    var addRemove = findViewById<ImageButton>(R.id.addRemove)
                    if (choice == "My Events") {
                        addRemove.setImageResource(R.drawable.remove)
                    } else {
                        addRemove.setImageResource(R.drawable.add)
                    }
                }
            }).start()
        }
    }

    // depending on view, the button will add an event to the users's list
    // or delete it from the user's list
    fun addRemoveEvent(view: View) {
        val db = DBHelper.EventDatabase.getDatabaseInstance(this)


        if (mChoice == "My Events") {
            scope.launch{
                db.userEventDao().deleteEvent(mId)
            }
        } else { // if (mChoice == "All Events"){
            scope.launch {
                var event: DBHelper.Event = db.allEventDao().getEvent(mId)
                mUser?.let { Log.d(TAG, it) }
                var userEvent =
                    mUser?.let {
                        DBHelper.UserEvent(event.id, event.startDate, event.discipline,
                            it
                        )
                    }
                Log.d(TAG, event.discipline)
                try {
                    if (userEvent != null) {
                        Log.d(TAG, userEvent.user)
                        db.userEventDao().insert(userEvent.id, userEvent.startDate, userEvent.discipline, userEvent.user)
                    }
                } catch (e: Exception) {
                    Log.d(TAG,e.toString())
                }

            }
        }
        finish()
    }

    fun back(view: View) {
        finish()
    }
}