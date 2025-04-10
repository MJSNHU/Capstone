package com.cs_499.capstone

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

// Array Adapter to list each event view
class EventsViewAdapter(context: Context, arrayList: List<Events>, choice: String, username: String) :
    ArrayAdapter<Events?>(context, 0, arrayList) {
    private val TAG = "EventsViewAdapter"
    private val mEvents: List<Events> = arrayList
    private val mChoice = choice
    private val mUsername = username

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var currentItemView = convertView

        if (currentItemView == null) {
            currentItemView =
                LayoutInflater.from(context).inflate(R.layout.row_listview_event, parent, false)
        }

        val currentEventPosition: Events? = getItem(position)

        var date = currentItemView!!.findViewById<TextView>(R.id.date)
        if (currentEventPosition != null) {
            date.text = stringFromEpoch(currentEventPosition.getDate())
        }

        var event = currentItemView.findViewById<TextView>(R.id.event)
        if (currentEventPosition != null) {
            event.text = currentEventPosition.getEvent()
        }

        return currentItemView
    }

    // return the length of the events list
    override fun getCount(): Int {
         return mEvents.size
    }

    // get the id of the item clicked
    override fun getItemId(arg0: Int): Long {
        //TODO: Figure out why this is being called twice on items further down the list

        Log.d(TAG, "getItem: " + mEvents[arg0].getEId())
        var event = Events(mEvents[arg0].getEId(), mEvents[arg0].getDate(), mEvents[arg0].getEvent(), mEvents[arg0].getUser())

        Log.d(TAG, event.getEId().toString())

        var intent= Intent(context, ViewFullEvent::class.java)
        intent.putExtra("eventId",event.getEId())
        intent.putExtra("choice", mChoice)
        intent.putExtra("username", mUsername)
        context.startActivity(intent)

        return mEvents[arg0].getEId()
    }


}