package com.sap.sailing.android.buoy.positioning.app.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.sap.sailing.android.buoy.positioning.app.provider.AnalyticsContract.CheckinUriColumns;
import com.sap.sailing.android.buoy.positioning.app.provider.AnalyticsContract.Leaderboard;
import com.sap.sailing.android.buoy.positioning.app.provider.AnalyticsContract.LeaderboardColumns;
import com.sap.sailing.android.buoy.positioning.app.provider.AnalyticsContract.Mark;
import com.sap.sailing.android.buoy.positioning.app.provider.AnalyticsContract.MarkColums;
import com.sap.sailing.android.buoy.positioning.app.provider.AnalyticsContract.MarkPingColumns;
import com.sap.sailing.android.shared.logging.ExLog;

public class AnalyticsDatabase extends SQLiteOpenHelper {

    private static final String TAG = AnalyticsDatabase.class.getName();
    
    private static final String DATABASE_NAME = "sap_sailing_analytics_buoy.db";
    
    private static final int VER_2015_RELEASE_1 = 1;
    private static final int CUR_DATABASE_VERSION = VER_2015_RELEASE_1;
    
    private final Context mContext;
    
    interface Tables {
        String LEADERBOARDS = "leaderboards";
        String MARKS = "marks";
        String MARK_PINGS = "mark_pings";
        String CHECKIN_URIS= "checkin_uris";
        
        String LEADERBOARDS_MARKS_JOINED = "marks LEFT JOIN leaderboards ON (" + Tables.MARKS + "." + Mark.MARK_CHECKIN_DIGEST 
        		+ " = " + Tables.LEADERBOARDS + "." + Leaderboard.LEADERBOARD_CHECKIN_DIGEST + " )";
    }
    
    public AnalyticsDatabase(Context context) {
        super(context, DATABASE_NAME, null, CUR_DATABASE_VERSION);
        mContext = context;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) { 
    	db.execSQL("CREATE TABLE " + Tables.LEADERBOARDS + " ("
    			+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
    			+ LeaderboardColumns.LEADERBOARD_CHECKIN_DIGEST + " TEXT, "
    			+ LeaderboardColumns.LEADERBOARD_NAME + " TEXT );");

        db.execSQL("CREATE TABLE " + Tables.CHECKIN_URIS + " ("
    			+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
    			+ CheckinUriColumns.CHECKIN_URI_CHECKIN_DIGEST + " TEXT, "
    			+ CheckinUriColumns.CHECKIN_URI_VALUE + " TEXT );");
        
        db.execSQL("CREATE TABLE " + Tables.MARKS + " ("
    			+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
    			+ MarkColums.MARK_CHECKIN_DIGEST + " TEXT,"
    			+ MarkColums.MARK_ID + " INTEGER,"
    			+ MarkColums.MARK_NAME + " TEXT,"
    			+ MarkColums.MARK_TYPE + " TEXT,"
    			+ MarkColums.MARK_CLASS_NAME + " TEXT );");
        
        db.execSQL("CREATE TABLE " + Tables.MARK_PINGS + " ("
    			+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
    			+ MarkPingColumns.MARK_ID + " INTEGER,"
    			+ MarkPingColumns.MARK_PING_TIMESTAMP + " TEXT,"
    			+ MarkPingColumns.MARK_PING_LATITUDE + " TEXT,"
    			+ MarkPingColumns.MARK_PING_LONGITUDE + " TEXT,"
    			+ MarkPingColumns.MARK_PING_ACCURACY + " REAL );");        
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       ExLog.i(mContext, TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
       
       int version = oldVersion;
       
       if (version != CUR_DATABASE_VERSION) {
           ExLog.i(mContext, TAG, "Upgrade unsuccessful - destroying old data during upgrade");
           
           db.execSQL("DROP TABLE IF EXISTS " + Tables.LEADERBOARDS);
           db.execSQL("DROP TABLE IF EXISTS " + Tables.CHECKIN_URIS);
           db.execSQL("DROP TABLE IF EXISTS " + Tables.MARKS);
           db.execSQL("DROP TABLE IF EXISTS " + Tables.MARK_PINGS);

           onCreate(db);
           version = CUR_DATABASE_VERSION;
       }
    }
    
    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }
}
