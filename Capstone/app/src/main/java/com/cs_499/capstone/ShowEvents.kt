package com.cs_499.capstone



import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cs_499.capstone.R.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlin.coroutines.CoroutineContext


class ShowEvents : AppCompatActivity() {

    private val coroutineContext: CoroutineContext = newSingleThreadContext("searchDB")
    private val scope = CoroutineScope(coroutineContext)
    private val TAG = "ShowEvents"
    private var mUsername: String = ""
    private var mMenu: Menu? = null
    private var mChoice: String = ""
    private var mAllEventsList: List<DBHelper.Event> = listOf()
    private var mEventsList: List<DBHelper.UserEvent> = listOf()
    private var mType = 0 // 0 to sort by date, 1 to sort by event


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(layout.activity_show_events)
        setSupportActionBar(findViewById(id.my_toolbar))
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }

        // Display username in header
        val usernameTextView: TextView = findViewById(id.UserName)
        var uDao = DBHelper.EventDatabase.getDatabaseInstance(this).userDao()
        scope.launch {
            Log.d(TAG, intent.getLongExtra("userId",-1).toString())
            mUsername = uDao.getUsername(intent.getLongExtra("userId", -1))
            usernameTextView.text = mUsername
        }

        // Show the list
        displayList()

    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "on resume")
        if (mChoice == "My Events") {
            displayList()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        // Inflate the menu; this adds items to the action bar
        menuInflater.inflate(R.menu.menu, menu)
        this.mMenu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection

        var choice = item.title
        mChoice = choice.toString()
        when (item.itemId) {
            id.action_toggle_list -> {

                if (item.title == "My Events") {
                    item.title = "All Events"

                } else if (item.title == "All Events") {
                    item.title = "My Events"

                }
                displayList()
                return true

            }

            id.action_search -> {
                val intent = Intent(this@ShowEvents, Search::class.java)
                intent.putExtra("searchTerm", "*")
                intent.putExtra("start", 0)
                intent.putExtra("end", 0)
                startActivityForResult(intent, 0)
                Log.d(TAG, "finished")

                return true
            }

            id.action_logout -> {
                finish()

            return true
        }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                // Get the result from intent
                var searchTerm = data?.getStringExtra("searchTerm")
                var start = data?.getLongExtra("start", 0)
                var end = data?.getLongExtra("end", 0)
                var ev: Events
                var eventsListView: ArrayList<Events>
                var listView: ListView = findViewById(R.id.EventList)
                var eventArrayAdapter: EventsViewAdapter
                scope.launch {
                    mAllEventsList = DBHelper.EventDatabase.getDatabaseInstance(this@ShowEvents).allEventDao().refinedSearch(searchTerm.toString(), start.toString().toLong(), end.toString().toLong())
                    mEventsList = convert(mAllEventsList, mUsername)
                    eventsListView = arrayListOf()

                    eventsListView = arrayListOf()
                    for (e: DBHelper.UserEvent in mEventsList) {
                        ev = Events(e.id, e.startDate, e.discipline, mUsername)
                        eventsListView.add(ev)
                    }

                    eventArrayAdapter =
                        EventsViewAdapter(this@ShowEvents, eventsListView, mChoice, mUsername)
                    Thread(kotlinx.coroutines.Runnable {
                        this@ShowEvents.runOnUiThread {

                            listView.adapter = eventArrayAdapter
                        }
                    }).start()
                }
            }
        }
    }

    private fun displayList() {

        var ev: Events
        var eventsListView: ArrayList<Events>
        var listView: ListView = findViewById(R.id.EventList)
        var eventArrayAdapter: EventsViewAdapter
        var bst = BinarySearchTree(mType)

        // Show all events
        if (mChoice != "My Events") {
                    scope.launch {
                    mAllEventsList =
                        DBHelper.EventDatabase.getDatabaseInstance(this@ShowEvents).allEventDao()
                            .getEvents()
                    mEventsList = convert(mAllEventsList, mUsername)
                    eventsListView = arrayListOf()

                    eventsListView = arrayListOf()
                    for (e: DBHelper.UserEvent in mEventsList) {
                        ev = Events(e.id, e.startDate, e.discipline, mUsername)
                        eventsListView.add(ev)
                    }

                    eventArrayAdapter =
                        EventsViewAdapter(this@ShowEvents, eventsListView, mChoice, mUsername)
                    Thread(kotlinx.coroutines.Runnable {
                        this@ShowEvents.runOnUiThread {

                            listView.adapter = eventArrayAdapter
                        }
                    }).start()

                }
        // show My Events
        } else {
            scope.launch {
                mEventsList =
                    DBHelper.EventDatabase.getDatabaseInstance(this@ShowEvents).userEventDao().getEvents(mUsername)
                for (e: DBHelper.UserEvent in mEventsList) {
                    bst.insert(e)
                }
                mEventsList = bst.inOrderTraversal(bst.root)
                eventsListView = arrayListOf()
                for (e: DBHelper.UserEvent in mEventsList) {
                    ev = Events(e.id, e.startDate, e.discipline, mUsername)
                    eventsListView.add(ev)
                }

                eventArrayAdapter =
                    EventsViewAdapter(this@ShowEvents, eventsListView, mChoice, mUsername)

                Thread(kotlinx.coroutines.Runnable {
                    this@ShowEvents.runOnUiThread {

                        listView.adapter = eventArrayAdapter
                    }
                }).start()
            }
        }
    }
    // build binary search tree by time
    fun sortByTime(view: View) {
        mType = 0
        val bst = BinarySearchTree(mType)
        if(mChoice == "My Events") {
            for (e: DBHelper.UserEvent in mEventsList) {
                bst.insert(e)
            }
        } else {
            for (e: DBHelper.UserEvent in mEventsList) {
                bst.insert(DBHelper.UserEvent(e.id, e.startDate, e.discipline, mUsername))
            }

        }
        // get list in order of time
        mEventsList = bst.inOrderTraversal(bst.root)
        var ev: Events
        var eventsListView: ArrayList<Events> = arrayListOf()
        var listView: ListView = findViewById(R.id.EventList)
        var eventArrayAdapter: EventsViewAdapter
        for (e: DBHelper.UserEvent in mEventsList) {
            ev = Events(e.id, e.startDate, e.discipline, mUsername)
            eventsListView.add(ev)
        }
        eventArrayAdapter =
            EventsViewAdapter(this@ShowEvents, eventsListView, mChoice, mUsername)
        listView.adapter = eventArrayAdapter

    }

    // build binary search tree in order of event
    fun sortByDiscipline(view: View) {
        mType = 1
        val bst = BinarySearchTree(mType)
        if(mChoice == "My Events") {
            for (e: DBHelper.UserEvent in mEventsList) {
                bst.insert(e)
            }
        } else {
            for (e: DBHelper.UserEvent in mEventsList) {
            bst.insert(DBHelper.UserEvent(e.id, e.startDate, e.discipline, mUsername))
            }
        }

        // get list in order of event
        mEventsList = bst.inOrderTraversal(bst.root)
        var ev: Events
        var eventsListView: ArrayList<Events> = arrayListOf()
        var listView: ListView = findViewById(R.id.EventList)
        var eventArrayAdapter: EventsViewAdapter
        for (e: DBHelper.UserEvent in mEventsList) {
            ev = Events(e.id, e.startDate, e.discipline, mUsername)
            eventsListView.add(ev)
        }
        eventArrayAdapter =
            EventsViewAdapter(this@ShowEvents, eventsListView, mChoice, mUsername)
        listView.adapter = eventArrayAdapter

    }
}

