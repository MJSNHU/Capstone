package com.CS360.project3;

public class EventsView {

    private long ivE_id;
    private String mDate;
    private String mEvent;
    private boolean mChecked;

    public EventsView (long e_id, String date, String event, Boolean checked) {
        ivE_id = e_id;
        mDate = date;
        mEvent = event;
        mChecked = checked;
    }

    public long getE_id() {
        return ivE_id;
    }

    public String getDate() {return mDate;
    }

    public String getEvent() {
        return mEvent;
    }

    public boolean getChecked() {
        return mChecked;
    }
}
