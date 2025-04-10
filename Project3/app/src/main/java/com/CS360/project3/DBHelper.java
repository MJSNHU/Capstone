package com.CS360.project3;

import android.app.AlarmManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";
    private static final String DATABASE_NAME = "project3.db";
    private static final int VERSION = 1;
    private Context mContext;
    private String DATABASE_PATH;
    public static DBHelper mInstance = null;
    private static SQLiteDatabase mSQLdbWrite = null;
    private static SQLiteDatabase mSQLdbRead = null;


    final class UserTable {
        static final String TABLE = "users";
        static final String COL_ID = "_uid";
        static final String COL_USERNAME = "username";
        static final String COL_PASSWORD = "password";
    }

    final class EventTable {
        static final String TABLE = "events";
        static final String E_Id = "_eid";
        static final String U_Id = "_uid";
        static final String DATETIME = "datetime";
        static final String EVENT = "event";
        static final String DETAILS = "details";
        static final String ALARM = "alarm";
    }

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        this.mContext = context;
        this.DATABASE_PATH = mContext.getDatabasePath(DATABASE_NAME).getPath();
    }

    public static DBHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DBHelper(context);
            mSQLdbWrite = mInstance.getWritableDatabase();
            mSQLdbRead = mInstance.getReadableDatabase();

        }
        return mInstance;
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG, "onCreate");


    }

    @Override
    public void onUpgrade(SQLiteDatabase SQLdb, int oldVersion, int newVersion) {
        SQLdb.execSQL("drop table if exists " + UserTable.TABLE);
        SQLdb.execSQL("drop table if exists " + EventTable.TABLE);
        onCreate(SQLdb);
    }

    public long createDatabase() {
        Log.d("Database Path", DATABASE_PATH);
        long result;
        try {

            getSQLdbWrite().execSQL("CREATE TABLE " + UserTable.TABLE + "(" +
                    UserTable.COL_ID + " integer primary key autoincrement, " +
                    UserTable.COL_USERNAME + " TEXT NOT NULL, " +
                    UserTable.COL_PASSWORD + " TEXT NOT NULL);");

            getSQLdbWrite().execSQL("CREATE TABLE " + EventTable.TABLE + "(" +
                    EventTable.E_Id + " INTEGER primary key autoincrement, " +
                    EventTable.U_Id + " INTEGER NOT NULL," +
                    EventTable.DATETIME + " DATETIME NOT NULL, " +
                    EventTable.EVENT + " TEXT NOT NULL, " +
                    EventTable.DETAILS + " MEDIUMTEXT NOT NULL, "+
                    EventTable.ALARM + " INTEGER NOT NULL);");

            Log.d(TAG, "database created");
            return 1;
        } catch (Exception e) {
            Log.d(TAG, "Create database: " + e.getMessage());

            if (e.getMessage().contains("already exists")) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    public SQLiteDatabase getSQLdbWrite() {
        if (mSQLdbWrite == null) {
            mSQLdbWrite = mInstance.getWritableDatabase();
        }
        return mSQLdbWrite;
    }

    public static SQLiteDatabase getSQLdbRead() {
        if (mSQLdbRead == null) {
            mSQLdbRead = mInstance.getReadableDatabase();
        }
        return mSQLdbRead;
    }

    public static String getUsername(long u_id){

        long result = 0;
        String username = "" ;
        try {
            String sql = "SELECT * from " + UserTable.TABLE + " where _uid = ?";
            Cursor c = getSQLdbRead().rawQuery(sql, new String[]{String.valueOf(u_id)});

            result = c.getCount();
            if (result == 0) {
                Log.d(TAG, "no user found, uid" + u_id);
            } else if (result == 1) {
                c.moveToFirst();
                username = c.getString(2);
            } else {
                Log.d(TAG, "err: more than one username " + c.getString(0));
            }
            c.close();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return username;
    }

    public static long retrieveUser(User user) {

        long result = 0;
        try {
            String sql = "SELECT * from " + UserTable.TABLE + " where username = ?";
            Cursor c = getSQLdbRead().rawQuery(sql, new String[]{user.username});
            result = c.getCount();
            c.close();

            if (result < 0) {
                Log.d(TAG, "err: cursor less than 0, should never get here");
            } else if (result == 0) {
                Log.d(TAG, "no such user");
            } else if (result == 1) {
                Log.d(TAG, "user found");
            } else {
                Log.d(TAG, "err: user exists more than once");
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return result;
    }

    public static long verifyUser (User user) {
        long result;
        long uid = -1;
        String u = "";
        String p = "";
        try {
            String sql = "SELECT * from " + UserTable.TABLE + " where username = ?";
            Cursor c = getSQLdbRead().rawQuery(sql, new String[]{user.username});
            result = c.getCount();
            if (result > 1) {
                Log.d(TAG, "err: more than one user");
            }
            if (c.moveToFirst()) {
                do {
                    uid = c.getLong(0);
                    u = c.getString(1);
                    p = c.getString(2);
                    Log.d(TAG, "user: " + uid + " " + u + ", " + p);
                } while (c.moveToNext());
            }
            c.close();
        } catch (Exception e) {
            Log.d (TAG, e.getMessage());
        }

        Log.d(TAG, "Password check: " + String.valueOf(user.password.compareTo(p)));

        if (user.password.compareTo(p) == 0) {
            Log.d(TAG, "passwords match");
            return uid;
        } else {
            Log.d(TAG, "passwords don't match");
            return -2;
        }
    }

    private long insertUser(User user) {
        long result = -1;
        try {
            String sql = "SELECT * from " + UserTable.TABLE + " where username = ?";
            Cursor c = getSQLdbRead().rawQuery(sql, new String[]{user.username});
            if (c.getCount() > 0) {
                Log.d(TAG, "user exists");
                result = -1;
            } else {
                ContentValues contentValues = new ContentValues();
                contentValues.put(UserTable.COL_USERNAME, user.username);
                contentValues.put(UserTable.COL_PASSWORD, user.password);

                result = getSQLdbWrite().insert(UserTable.TABLE, null, contentValues);
            }
        } catch (Exception e) {
            Log.d(TAG, "Error inserting User: " + e.getMessage());
        }

        return result;
    }

    private long insertEvent(Event event) {
        long result = -1;

        Log.d(TAG, "insertEvent" + event.event + " " + event.details);
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(EventTable.U_Id, event.u_id);
            contentValues.put(EventTable.DATETIME, event.date);
            contentValues.put(EventTable.EVENT, event.event);
            contentValues.put(EventTable.DETAILS, event.details);
            contentValues.put(EventTable.ALARM, event.alarm);

            result = getSQLdbWrite().insert(EventTable.TABLE, null, contentValues);
        } catch (Exception e) {
            Log.d(TAG, "Insert event: " + e.getMessage());
        }
        return result;
    }

    private long deleteEvent(long e_id) {

        int result = -1;
        //try {
            Log.d(TAG, "deleting event");
            String sql = "_eid = ?";
            result = getSQLdbWrite().delete(EventTable.TABLE, sql, new String[] {String.valueOf(e_id)});

        //} catch (Exception e) {
        //    Log.d(TAG, "Delete event: " + e.getMessage());
       // }

        return result;
    }

    public long addUser(User user) {
        long result = insertUser(user);
        Log.d(TAG, "insert user id: " + result + " " + user.username + " " + user.password);
        if (result < 0) {
            Log.d(TAG, "insertUser fail");
        } else if (result == 0) {
            Log.d(TAG, "user avaliable");
        } else {
            Log.d(TAG, "insertUser fail greater than 1");
        }
        return result;
    }

    public long addEvent(Event event) {

        long id = insertEvent(event);
        if (id <=0) {
            Log.d(TAG, "add event id: " + id);
        }

        return id;
    }

    public long removeEvent(long e_id) {

        long id = deleteEvent(e_id);
        Log.d(TAG, String.valueOf(id));
        if (id == 0){
            Log.d(TAG, "delete fail");

        } else {
            Log.d(TAG, "delete success");
            Toast toast = new Toast(mContext);
            toast.makeText(mContext, "Event Deleted", Toast.LENGTH_LONG);

        }
        return id;
    }

    public static ArrayList<Event> getEvents(long u_id){
        ArrayList<Event> mEvents = new ArrayList<Event>();
        try {

            String sql = "SELECT * from " + EventTable.TABLE + " where _uid = ? ORDER BY datetime";
            Cursor c = getSQLdbRead().rawQuery(sql, new String[]{String.valueOf(u_id)});
            long result = c.getCount();
            if (result > 1) {
                Log.d(TAG, "err: more than one user");
            }
            if (c.moveToFirst()) {

                do {
                    Event e = new Event();

                    e.e_id = c.getLong(0);
                    e.u_id = c.getLong(1);
                    e.date = c.getLong(2);
                    e.event = c.getString(3);
                    e.details = c.getString(4);
                    e.alarm = c.getInt(5);
                    if (e.alarm == 1) {
                        Log.d(TAG, "schedule Message");
                    } else {
                        Log.d(TAG, "remove Message");
                    }
                    Log.d(TAG, "event: " + e.e_id + " " + e.u_id + " " + e.date + " " + e.event + " " + e.alarm);
                    mEvents.add(e);
                } while (c.moveToNext());
            }
            c.close();
        } catch (Exception e) {
            Log.d (TAG, "Get events: " + e.getMessage());
        }
        Log.d(TAG, u_id + " " + mEvents.size());
        return mEvents;
    }

    public long updateEvent(Event e) {

        ContentValues cv = new ContentValues();
        cv.put(EventTable.DATETIME, e.date);
        cv.put(EventTable.EVENT, e.event);
        cv.put(EventTable.DETAILS, e.details);
        cv.put(EventTable.ALARM, e.alarm);
        Log.d(TAG, cv.toString());
        Log.d(TAG, "Datetime: " + e.date);
        long id = getSQLdbWrite().update(EventTable.TABLE, cv, "_eid=?", new String[]{String.valueOf(e.e_id)});
        Log.d(TAG, String.valueOf(id));
        return id;
    }


}

