package com.cs_499.capstone

//Events Object

class Events(id: Long, startDate: Long, discipline: String, user: String) {
    private var meId: Long = id
    private var mDate: Long = startDate
    private var mEvent: String = discipline
    private var mUser: String = user

    fun getEId(): Long { return meId }

    fun getDate(): Long { return mDate }

    fun getEvent(): String { return mEvent }

    fun getUser(): String { return mUser}

}