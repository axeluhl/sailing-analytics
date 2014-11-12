package com.sap.sailing.android.tracking.app.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.provider.AnalyticsContract.Competitor;
import com.sap.sailing.android.shared.provider.AnalyticsContract.Event;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.CompetitorColumns;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.EventColumns;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.MessageColumns;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.SensorGpsColumns;

public class AnalyticsDatabase extends SQLiteOpenHelper {

    private static final String TAG = AnalyticsDatabase.class.getName();
    
    private static final String DATABASE_NAME = "sap_sailing_analytics.db";
    
    private static final int VER_2014_RELEASE_1 = 1;
    private static final int CUR_DATABASE_VERSION = VER_2014_RELEASE_1;
    
    private final Context mContext;
    
    interface Tables {
        String COMPETITORS = "competitors";
        String EVENTS = "events";
        String EVENTS_COMPETITORS = "events_competitors";
        String SENSOR_GPS = "sensor_gps";
        String MESSAGES = "messages";
        
        String EVENTS_JOIN_COMPETITOR = "events_competitors "
                + "LEFT OUTER JOIN competitors ON events_competitors.competitor_id = competitors.competitor_id "
                + "LEFT OUTER JOIN events ON events_competitors.event_id = events.event_id ";
    }
    
    private interface Triggers {
        String COMPETITORS_DELETE_00 = "competitors_delete_00";
        String EVENTS_DELETE_00 = "events_delete_00";
    }
    
    public interface EventCompetitor {
        String EVENT_ID = EventColumns.EVENT_ID;
        String COMPETITOR_ID = CompetitorColumns.COMPETITOR_ID;
    }
    
    public interface Qualified {
        String EVENT_COMPETITOR_EVENT_ID = Tables.EVENTS_COMPETITORS + "." + EventCompetitor.EVENT_ID;
        String EVENT_COMPETITOR_COMPETITOR_ID = Tables.EVENTS_COMPETITORS + "." + EventCompetitor.COMPETITOR_ID;
    }
    public interface References {
        String COMPETITOR_ID = "REFERENCES " + Tables.COMPETITORS + "." + Competitor.COMPETITOR_ID;
        String EVENT_ID = "REFERENCES " + Tables.EVENTS + "." + Event.EVENT_ID;
    }
    
    public AnalyticsDatabase(Context context) {
        super(context, DATABASE_NAME, null, CUR_DATABASE_VERSION);
        mContext = context;
    }
    
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA recursive_triggers = true");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {        
        db.execSQL("CREATE TABLE " + Tables.COMPETITORS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CompetitorColumns.COMPETITOR_ID + " TEXT NOT NULL, "
                + CompetitorColumns.COMPETITOR_NAME + " TEXT NOT NULL, "
                + CompetitorColumns.COMPETITOR_PROFILE_IMAGE_URL + " TEXT, "
                + "UNIQUE (" + CompetitorColumns.COMPETITOR_ID + ") ON CONFLICT REPLACE)");
        
        db.execSQL("CREATE TABLE " + Tables.EVENTS + " ( "
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + EventColumns.EVENT_ID + " TEXT NOT NULL, "
                + EventColumns.EVENT_TITLE + " TEXT NOT NULL, "
                + EventColumns.EVENT_DATE_START + " INTEGER, "
                + EventColumns.EVENT_DATE_END + " INTEGER, "
                + EventColumns.EVENT_SERVER + " STRING, "
                + "UNIQUE (" + EventColumns.EVENT_ID + ") ON CONFLICT REPLACE)");
        
        db.execSQL("CREATE TABLE " + Tables.EVENTS_COMPETITORS + " ( "
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + EventColumns.EVENT_ID + " TEXT NOT NULL, "
                + CompetitorColumns.COMPETITOR_ID + " TEXT NOT NULL "
                + ")");
        
        db.execSQL("CREATE TRIGGER " + Triggers.COMPETITORS_DELETE_00 + " BEFORE DELETE ON "
                + Tables.COMPETITORS + " BEGIN DELETE FROM " + Tables.EVENTS_COMPETITORS + " "
                + " WHERE " + Qualified.EVENT_COMPETITOR_COMPETITOR_ID + " = old." + CompetitorColumns.COMPETITOR_ID
                + ";" + " END;");
        
        db.execSQL("CREATE TRIGGER " + Triggers.EVENTS_DELETE_00 + " BEFORE DELETE ON "
                + Tables.EVENTS + " BEGIN DELETE FROM " + Tables.EVENTS_COMPETITORS + " "
                + " WHERE " + Qualified.EVENT_COMPETITOR_EVENT_ID + " = old." + EventColumns.EVENT_ID
                + ";" + " END;");
        
        db.execSQL("CREATE TABLE " + Tables.MESSAGES + " ( "
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MessageColumns.MESSAGE_URL + " TEXT NOT NULL, "
                + MessageColumns.MESSAGE_PAYLOAD + " TEXT, "
                + MessageColumns.MESSAGE_CALLBACK_CLASS_STRING + " TEXT, "
                + MessageColumns.MESSAGE_CALLBACK_PAYLOAD + " TEXT "
                + ")");
        
        db.execSQL("CREATE TABLE " + Tables.SENSOR_GPS + " ( "
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SensorGpsColumns.GPS_ACCURACY + " TEXT, "
                + SensorGpsColumns.GPS_ALTITUDE + " TEXT, "
                + SensorGpsColumns.GPS_BEARING + " TEXT, "
                + SensorGpsColumns.GPS_DEVICE + " TEXT, "
                + SensorGpsColumns.GPS_ELAPSED_REALTIME + " TEXT, "
                + SensorGpsColumns.GPS_LATITUDE + " TEXT, "
                + SensorGpsColumns.GPS_LONGITUDE + " TEXT, "
                + SensorGpsColumns.GPS_PROVIDER + " TEXT, "
                + SensorGpsColumns.GPS_SPEED + " TEXT, "
                + SensorGpsColumns.GPS_SYNCED + " INTEGER DEFAULT 0, "
                + SensorGpsColumns.GPS_TIME + " TEXT "
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       ExLog.i(mContext, TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
       
       int version = oldVersion;
       
       if (version != CUR_DATABASE_VERSION) {
           ExLog.i(mContext, TAG, "Upgrade unsuccessful - destroying old data during upgrade");
           
           db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.COMPETITORS_DELETE_00);
           db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.EVENTS_DELETE_00);
           
           db.execSQL("DROP TABLE IF EXISTS " + Tables.COMPETITORS);
           db.execSQL("DROP TABLE IF EXISTS " + Tables.EVENTS);
           db.execSQL("DROP TABLE IF EXISTS " + Tables.EVENTS_COMPETITORS);
           db.execSQL("DROP TABLE IF EXISTS " + Tables.MESSAGES);
           db.execSQL("DROP TABLE IF EXISTS " + Tables.SENSOR_GPS);
           
           onCreate(db);
           version = CUR_DATABASE_VERSION;
       }
    }
    
    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }
}
