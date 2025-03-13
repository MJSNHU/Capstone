package com.cs_499.capstone

import android.content.Context
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import com.cs_499.capstone.DBHelper.Event
import java.io.File
import java.io.InputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class DBHelper()  {

    @Entity(tableName = "events")
    data class Event(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        @ColumnInfo(name= "start_date") val startDate: Long?,
        @ColumnInfo(name= "end_date") val endDate: Long?,
        @ColumnInfo(name= "day") val day: String?,
        @ColumnInfo(name= "status") val status: String?,
        @ColumnInfo(name= "discipline") val discipline: String?,
        @ColumnInfo(name= "discipline_code") val disciplineCode: String?,
        @ColumnInfo(name= "event") val event: String?,
        @ColumnInfo(name= "event_medal") val eventMedal: String?,
        @ColumnInfo(name= "phase") val phase: String?,
        @ColumnInfo(name= "gender") val gender: String?,
        @ColumnInfo(name= "event_type") val eventType: String?,
        @ColumnInfo(name= "venue") val venue: String?,
        @ColumnInfo(name= "venue_code") val venueCode: String?,
        @ColumnInfo(name= "location_description") val locationDescription: String?,
        @ColumnInfo(name= "location_code") val locationCode: String?,
        @ColumnInfo(name= "url") val url: String?,
      )

    @Dao
    interface EventDao {
        // Insert a list of events into the database
        @Insert
        suspend fun insertAll(events: List<Event>)

        @Query("SELECT * FROM events")
        suspend fun getAllEvents(): List<Event>

       @Query("SELECT EXISTS(SELECT * FROM events LIMIT 1)")
       suspend fun hasEventTable(): Boolean?
    }

    @Database(entities = [Event::class], version = 1)
    abstract class EventDatabase : RoomDatabase() {
        abstract fun eventDao(): EventDao

        companion object DatabaseProvider {
            private var INSTANCE: EventDatabase? = null

            fun getDatabaseInstance(context: Context): EventDatabase {
                if (INSTANCE == null) {
                    synchronized(this) {
                       INSTANCE = Room.databaseBuilder(
                           context.applicationContext,
                           EventDatabase::class.java,
                           "olympic_events.db"
                       ).build()
                    }
                }
                return INSTANCE!!
            }
        }
    }
}

fun loadCSV(ins: InputStream): List<Event> {
    Log.d("DBHelper", "loadCSV")
    // expand list components
    operator fun <T>List<T>.component6(): T = get(5)
    operator fun <T>List<T>.component7(): T = get(6)
    operator fun <T>List<T>.component8(): T = get(7)
    operator fun <T>List<T>.component9(): T = get(8)
    operator fun <T>List<T>.component10(): T = get(9)
    operator fun <T>List<T>.component11(): T = get(10)
    operator fun <T>List<T>.component12(): T = get(11)
    operator fun <T>List<T>.component13(): T = get(12)
    operator fun <T>List<T>.component14(): T = get(13)
    operator fun <T>List<T>.component15(): T = get(14)
    operator fun <T>List<T>.component16(): T = get(15)

    val reader = ins.bufferedReader()
    val header = reader.readLine()
    return reader.lineSequence()
        .filter {it.isNotBlank() }
        .map {
            val (start_date,
                end_date,
                day,
                status,
                discipline,
                discipline_code,
                event,
                event_medal,
                phase,
                gender,
                event_type,
                venue,
                venue_code,
                location_description,
                location_code,
                url) = it.split(",", ignoreCase=false, limit=0)
            Event(0, epochFromString(start_date), epochFromString(end_date), day, status, discipline, discipline_code, event, event_medal, phase, gender, event_type, venue, venue_code, location_description, location_code, url)
            }.toList()



}

fun stringFromEpoch(epoch: Long) : String {

    val instant = Instant.ofEpochSecond(epoch)
    val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    Log.d("DBHelper", dateTime.format(formatter))
    return dateTime.format(formatter)
}

fun epochFromString (date: String) : Long{
    val cutDate = date.split("+")[0]
    if (cutDate.equals("")) {
        return 0
    }
    val dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault())
    val zdt = ZonedDateTime.parse(cutDate, dtf)
    val instant = zdt.toInstant()

    return instant.getEpochSecond()
}

fun getEventsFromCSV(context: Context) : List<Event> {

    var events: List<Event> = listOf<Event>()
    var filename: String = "/data/user/0/com.cs_499.capstone/cache/schedules.csv"

    try {
        Log.d("DBHelper", filename)
        val ins: InputStream = File(filename).inputStream()
        Log.d("DBHelper", "LoadingCSV")
        events = loadCSV(ins)

    } catch (e: Exception) {
        Log.d("DBHelper Error", e.toString())
    }
    return events
}