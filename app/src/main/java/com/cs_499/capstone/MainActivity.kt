package com.cs_499.capstone


import android.os.Bundle
import android.util.Log
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
    lateinit var dao: DBHelper.EventDao
    private val coroutineContext: CoroutineContext = newSingleThreadContext("loadDB")
    private val scope = CoroutineScope(coroutineContext)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }
        val events = getEventsFromCSV(applicationContext)
        db = DBHelper.EventDatabase.getDatabaseInstance(this)
        dao = db.eventDao()

        scope.launch() {
            if (dao.hasEventTable() == false) {
                dao.insertAll(events)
            }
        }

      /*  scope.launch() {
            val e: List<DBHelper.Event> = dao.getAllEvents()
            for (event: DBHelper.Event in e) {
                Log.d("MainActivity", event.discipline.toString())
            }
        }
      */

    }
}