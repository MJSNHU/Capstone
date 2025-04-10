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
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cs_499.capstone.DBHelper.Event
import java.io.InputStream
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


class DBHelper()  {

    //Entities set up columns for each table named

    @Entity(tableName = "events")
    data class Event(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        @ColumnInfo(name= "start_date") val startDate: Long,
        @ColumnInfo(name= "end_date") val endDate: Long,
        @ColumnInfo(name= "day") val day: String,
        @ColumnInfo(name= "status") val status: String,
        @ColumnInfo(name= "discipline") val discipline: String,
        @ColumnInfo(name= "discipline_code") val disciplineCode: String,
        @ColumnInfo(name= "event") val event: String,
        @ColumnInfo(name= "event_medal") val eventMedal: String,
        @ColumnInfo(name= "phase") val phase: String,
        @ColumnInfo(name= "gender") val gender: String,
        @ColumnInfo(name= "event_type") val eventType: String,
        @ColumnInfo(name= "venue") val venue: String,
        @ColumnInfo(name= "venue_code") val venueCode: String,
        @ColumnInfo(name= "location_description") val locationDescription: String,
        @ColumnInfo(name= "location_code") val locationCode: String,
        @ColumnInfo(name= "url") val url: String,
      )

    @Entity(tableName = "user_events")
    data class UserEvent(
        @PrimaryKey var id: Long,
        @ColumnInfo(name= "start_date") val startDate: Long,
        @ColumnInfo(name= "discipline") val discipline: String,
        @ColumnInfo(name= "user") val user: String,
    )


    @Entity(tableName = "users")
    data class User(
        @PrimaryKey(autoGenerate = true) var id: Long = 0,
        @ColumnInfo(name= "username") var username: String,
        @ColumnInfo(name= "salt") var salt: String,
        @ColumnInfo(name= "password") var password: String
    )

    //Daos define a set of interfaces to the database
    @Dao
    interface UserEventDao {
        // Insert an event into the database
        @Query ("INSERT OR IGNORE INTO user_events(id, start_date, discipline, user) VALUES(:eventID, :startDate, :discipline, :user)")
        suspend fun insert(eventID: Long, startDate: Long, discipline: String, user: String)

        @Query("SELECT * FROM user_events WHERE \"user\"=:username")
        suspend fun getEvents(username: String): List<UserEvent>

        @Query("SELECT EXISTS(SELECT * FROM user_events LIMIT 1)")
        suspend fun hasEventTable(): Boolean?

        @Query("DELETE FROM user_events WHERE id=:id")
        suspend fun deleteEvent(id: Long)
    }

    @Dao
    interface AllEventDao {
        @Insert
        suspend fun insertAll(events: List<Event>)

        @Query("SELECT * FROM events")
        suspend fun getEvents(): List<Event>

        @Query("SELECT EXISTS(SELECT * FROM events LIMIT 1)")
        suspend fun hasEventTable(): Boolean?

        @Query("SELECT * FROM events WHERE id=:eId")
        suspend fun getEvent(eId: Long): Event

        @Query("SELECT * FROM events WHERE discipline LIKE :term AND start_date >= :start AND end_date <= :end")
        suspend fun refinedSearch(term: String, start: Long, end: Long): List<Event>
    }

    @Dao
    interface UserDao {
        //
        @Insert
        suspend fun insertUser(users: List<User>)

        @Query("SELECT * FROM user_events")
        suspend fun getEvents(): List<UserEvent>

        @Query("SELECT id FROM users WHERE username=:username")
        suspend fun retrieveId(username: String): List<Long>

        @Query("SELECT EXISTS(SELECT * FROM users LIMIT 1)")
        suspend fun hasUsersTable(): Boolean?

        @Query("SELECT salt from users where username=:username")
        suspend fun getSalt(username: String): String

        @Query("SELECT * FROM users WHERE username=:username AND password=:password")
        suspend fun login(username: String, password: String): List<User>

        @Query("Select username FROM users WHERE id=:id")
        suspend fun getUsername(id: Long): String
    }

    // Set up the initial database
    @Database(entities = [Event::class, User::class, UserEvent::class], version = 3)
    abstract class EventDatabase : RoomDatabase() {
        abstract fun allEventDao(): AllEventDao
        abstract fun userDao(): UserDao
        abstract fun userEventDao(): UserEventDao

        companion object DatabaseProvider {
            private var INSTANCE: EventDatabase? = null

            // ensure only one instance of the database is used
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

// parse the input stream and return a list of Events
fun loadCSV(ins: InputStream): List<Event> {

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

    // not used; just reads the first line of column names
    val header = reader.readLine()

    // read event from csv into Event
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
            DBHelper.Event(0, epochFromString(start_date), epochFromString(end_date), day, status, discipline, discipline_code, event, event_medal, phase, gender, event_type, venue, venue_code, location_description, location_code, url)
            }.toList()
}

//convert Epoch time to a string
fun stringFromEpoch(epoch: Long) : String {
    val instant = Instant.ofEpochSecond(epoch)
    val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")
    return dateTime.format(formatter)
}

// convert String date to epic time
fun epochFromString (date: String) : Long{
    //remove the timezone difference //
    //Log.d("DBHelper", date)
    val cutDate = date.split("+")[0]
    if (cutDate.equals("")) {
        return 0
    }
    val dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault())
    val zdt = ZonedDateTime.parse(cutDate, dtf)
    val instant = zdt.toInstant()

    return instant.epochSecond
}

//get a list of events from the asset csv
fun getEventsFromCSV(context: Context) : List<Event> {
    val TAG = "DBHelper"
    var events: List<Event> = listOf()
    val ins: InputStream = context.assets.open("schedules.csv")

    try {
        Log.d(TAG, "LoadingCSV")
        events = loadCSV(ins)
    } catch (e: Exception) {
        Log.d("DBHelper Error", e.toString())
    }
    return events
}

@OptIn(ExperimentalStdlibApi::class)
fun randomSalt(): String {
    val r = SecureRandom()
    val salt = ByteArray(16)
    r.nextBytes(salt)
    return salt.toHexString()
}

@OptIn(ExperimentalStdlibApi::class)
fun hashPass(pass: String, saltHex: String): String {

    val ALGORITHM = "PBKDF2WithHmacSHA512"
    val ITERATIONS = 120_000
    val KEY_LENGTH = 256

    var salt = saltHex.hexToByteArray()

    val factory: SecretKeyFactory = SecretKeyFactory.getInstance(ALGORITHM)
    val spec: KeySpec = PBEKeySpec(pass.toCharArray(), salt, ITERATIONS, KEY_LENGTH)

    val key: SecretKey = factory.generateSecret(spec)
    val hash: ByteArray = key.encoded

    return hash.toHexString()

}

fun convert(allEventsList: List<DBHelper.Event>, username: String): List<DBHelper.UserEvent> {
    var userEventsList: ArrayList<DBHelper.UserEvent> = arrayListOf()
    var event: DBHelper.UserEvent

    for (e: DBHelper.Event in allEventsList) {
        event = DBHelper.UserEvent(e.id, e.startDate, e.discipline, username)
        userEventsList.add(event)
    }

    return userEventsList
}