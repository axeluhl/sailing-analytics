package com.sap.sailing.android.buoy.positioning.app.provider;

import com.sap.sailing.android.buoy.positioning.app.provider.AnalyticsContract.CheckinUri;
import com.sap.sailing.android.buoy.positioning.app.provider.AnalyticsContract.Leaderboard;
import com.sap.sailing.android.buoy.positioning.app.provider.AnalyticsContract.Mark;
import com.sap.sailing.android.buoy.positioning.app.provider.AnalyticsContract.MarkPing;
import com.sap.sailing.android.shared.logging.ExLog;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class AnalyticsDatabase extends SQLiteOpenHelper {

    private static final String TAG = AnalyticsDatabase.class.getName();

    private static final String DATABASE_NAME = "sap_sailing_analytics_buoy.db";

    private static final int DATABASE_VERSION = 2;

    private final Context mContext;

    interface Tables {
        String LEADERBOARDS = "leaderboards";
        String MARKS = "marks";
        String MARK_PINGS = "mark_pings";
        String CHECKIN_URIS = "checkin_uris";

        String MARKS_LEADERBOARDS_JOINED = "marks LEFT JOIN leaderboards ON (" + Tables.MARKS + "."
                + Mark.MARK_CHECKIN_DIGEST + " = " + Tables.LEADERBOARDS + "." + Leaderboard.LEADERBOARD_CHECKIN_DIGEST
                + " )";

        String LEADERBOARDS_MARKS_JOINED = "leaderboards LEFT JOIN marks ON (" + Tables.MARKS + "."
                + Mark.MARK_CHECKIN_DIGEST + " = " + Tables.LEADERBOARDS + "." + Leaderboard.LEADERBOARD_CHECKIN_DIGEST
                + " )";
    }

    public AnalyticsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.LEADERBOARDS + " (" + BaseColumns._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Leaderboard.LEADERBOARD_CHECKIN_DIGEST + " TEXT, "
                + Leaderboard.LEADERBOARD_SERVER_URL + " TEXT, " + Leaderboard.LEADERBOARD_NAME + " TEXT, "
                + Leaderboard.LEADERBOARD_DISPLAY_NAME + " TEXT );");

        db.execSQL("CREATE TABLE " + Tables.CHECKIN_URIS + " (" + BaseColumns._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + CheckinUri.CHECKIN_URI_CHECKIN_DIGEST + " TEXT, "
                + CheckinUri.CHECKIN_URI_VALUE + " TEXT );");

        db.execSQL("CREATE TABLE " + Tables.MARKS + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Mark.MARK_CHECKIN_DIGEST + " TEXT," + Mark.MARK_ID + " INTEGER," + Mark.MARK_NAME + " TEXT,"
                + Mark.MARK_TYPE + " TEXT," + Mark.MARK_CLASS_NAME + " TEXT );");

        db.execSQL("CREATE TABLE " + Tables.MARK_PINGS + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MarkPing.MARK_ID + " INTEGER," + MarkPing.MARK_PING_TIMESTAMP + " TEXT," + MarkPing.MARK_PING_LATITUDE
                + " TEXT," + MarkPing.MARK_PING_LONGITUDE + " TEXT," + MarkPing.MARK_PING_ACCURACY + " REAL );");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ExLog.i(mContext, TAG, "onUpgrade() called with: db = [" + db + "], oldVersion = [" + oldVersion
                + "], newVersion = [" + newVersion + "]");

        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + Tables.LEADERBOARDS + " ADD COLUMN " + Leaderboard.LEADERBOARD_DISPLAY_NAME
                    + " TEXT");
            db.execSQL("UPDATE " + Tables.LEADERBOARDS + " SET " + Leaderboard.LEADERBOARD_DISPLAY_NAME + " = "
                    + Leaderboard.LEADERBOARD_NAME);
        }
    }
}
