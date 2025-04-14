package com.CS360.project3;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import java.util.ArrayList;

public class EventsViewAdapter extends ArrayAdapter<EventsView> {
    private final String TAG = "EventsViewAdapter";
    private ArrayList<EventsView> mEvents;

    // invoke the suitable constructor of the ArrayAdapter class
    public EventsViewAdapter(@NonNull Context context, ArrayList<EventsView> arrayList) {

        // pass the context and arrayList for the super
        // constructor of the ArrayAdapter class
        super(context, 0, arrayList);
        mEvents = arrayList;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View currentItemView = convertView;

        if (currentItemView == null) {
            currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.row_listview_event, parent, false);
        }

        EventsView currentEventPosition = getItem(position);

        Log.d(TAG, "position: " + position);

        /*SwitchCompat toggle = currentItemView.findViewById(R.id.toggle);
        assert currentEventPosition != null;
        toggle.setChecked(currentEventPosition.getChecked());
        */
        TextView date = currentItemView.findViewById(R.id.date);
        date.setText(currentEventPosition.getDate());

        TextView event = currentItemView.findViewById(R.id.event);
        event.setText(currentEventPosition.getEvent());

        return currentItemView;
    }

    @Override
    public int getCount() {
        Log.d(TAG, "getCount: " + mEvents.size());
        return mEvents.size();
    }

    @Override
    public long getItemId(int arg0) {
        Log.d(TAG, "getItem");

        return mEvents.get(arg0).getE_id();
    }


}
