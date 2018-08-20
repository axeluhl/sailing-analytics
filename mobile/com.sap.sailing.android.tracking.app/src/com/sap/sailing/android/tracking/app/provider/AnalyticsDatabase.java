package com.sap.sailing.android.tracking.app.provider;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Checkin;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.CheckinColumns;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Competitor;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.CompetitorColumns;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Event;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.EventColumns;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Leaderboard;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.LeaderboardColumns;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class AnalyticsDatabase extends SQLiteOpenHelper {

    private static final String TAG = AnalyticsDatabase.class.getName();

    private static final String DATABASE_NAME = "sap_sailing_analytics.db";

    private static final int DATABASE_VERSION = 4;

    private static final String createMarkTable = "CREATE TABLE " + Tables.MARKS + " ("
        + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + AnalyticsContract.Mark.MARK_ID + " TEXT, "
        + AnalyticsContract.Mark.MARK_NAME + " TEXT, "
        + AnalyticsContract.Mark.MARK_CHECKIN_DIGEST + " TEXT);" ;

    private static final String createBoatTable = "CREATE TABLE " + Tables.BOATS + " ("
        + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + AnalyticsContract.Boat.BOAT_ID + " TEXT, "
        + AnalyticsContract.Boat.BOAT_NAME + " TEXT, "
        + AnalyticsContract.Boat.BOAT_CHECKIN_DIGEST + " TEXT,"
        + AnalyticsContract.Boat.BOAT_COLOR + " TEXT );" ;

    private final Context mContext;

    public interface Tables {
        String COMPETITORS = "competitors";
        String EVENTS = "events";
        String LEADERBOARDS = "leaderboards";
        String CHECKIN_URIS = "checkin_uris";
        String MARKS = "marks";
        String BOATS = "boats";

        String EVENTS_JOIN_LEADERBOARDS_JOIN_MARKS = Tables.LEADERBOARDS + " INNER JOIN " + Tables.EVENTS
            + " ON (" + Tables.LEADERBOARDS + "." + Leaderboard.LEADERBOARD_CHECKIN_DIGEST + " = " + Tables.EVENTS
            + "." + Event.EVENT_CHECKIN_DIGEST + ") " + " INNER JOIN " + Tables.MARKS + " ON ("
            + Tables.LEADERBOARDS + "." + Leaderboard.LEADERBOARD_CHECKIN_DIGEST + " = " + Tables.MARKS + "."
            + AnalyticsContract.Mark.MARK_CHECKIN_DIGEST + ") ";

        String EVENTS_JOIN_LEADERBOARDS_JOIN_COMPETITORS = Tables.LEADERBOARDS + " INNER JOIN " + Tables.EVENTS
                + " ON (" + Tables.LEADERBOARDS + "." + Leaderboard.LEADERBOARD_CHECKIN_DIGEST + " = " + Tables.EVENTS
                + "." + Event.EVENT_CHECKIN_DIGEST + ") " + " INNER JOIN " + Tables.COMPETITORS + " ON ("
                + Tables.LEADERBOARDS + "." + Leaderboard.LEADERBOARD_CHECKIN_DIGEST + " = " + Tables.COMPETITORS + "."
                + Competitor.COMPETITOR_CHECKIN_DIGEST + ") ";

        String LEADERBOARDS_JOIN_EVENTS_JOIN_COMPETITORS_JOIN_MARKS_JOINS_BOATS = Tables.LEADERBOARDS + " INNER JOIN " + Tables.EVENTS
            + " ON (" + Tables.LEADERBOARDS + "." + Leaderboard.LEADERBOARD_CHECKIN_DIGEST + " = " + Tables.EVENTS
            + "." + Event.EVENT_CHECKIN_DIGEST + ") " + " INNER JOIN " + Tables.CHECKIN_URIS + " ON ("
            + Tables.LEADERBOARDS + "." + Leaderboard.LEADERBOARD_CHECKIN_DIGEST + " = " + Tables.CHECKIN_URIS + "."
            + Checkin.CHECKIN_URI_CHECKIN_DIGEST + ") "
            + " LEFT OUTER JOIN " + Tables.COMPETITORS + " ON ("
            + Tables.LEADERBOARDS + "." + Leaderboard.LEADERBOARD_CHECKIN_DIGEST + " = " + Tables.COMPETITORS + "."
            + Competitor.COMPETITOR_CHECKIN_DIGEST + ") "
            + " LEFT OUTER JOIN " + Tables.MARKS + " ON ("
            + Tables.LEADERBOARDS + "." + Leaderboard.LEADERBOARD_CHECKIN_DIGEST + " = " + Tables.MARKS + "."
            + AnalyticsContract.Mark.MARK_CHECKIN_DIGEST + ") "
            + " LEFT OUTER JOIN " + Tables.BOATS + " ON ("
            + Tables.LEADERBOARDS + "." + Leaderboard.LEADERBOARD_CHECKIN_DIGEST + " = " + Tables.BOATS + "."
            + AnalyticsContract.Boat.BOAT_CHECKIN_DIGEST + ") ";
    }

    public AnalyticsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL("CREATE TABLE " + Tables.LEADERBOARDS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LeaderboardColumns.LEADERBOARD_CHECKIN_DIGEST + " TEXT, "
                + LeaderboardColumns.LEADERBOARD_NAME + " TEXT, "
                + LeaderboardColumns.LEADERBOARD_DISPLAY_NAME + " TEXT);");

            db.execSQL("CREATE TABLE " + Tables.CHECKIN_URIS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CheckinColumns.CHECKIN_URI_CHECKIN_DIGEST + " TEXT, "
                + CheckinColumns.CHECKIN_URI_VALUE + " TEXT, "
                + CheckinColumns.CHECKIN_TYPE + " INTEGER );");

            db.execSQL("CREATE TABLE " + Tables.COMPETITORS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CompetitorColumns.COMPETITOR_ID + " TEXT, "
                + CompetitorColumns.COMPETITOR_DISPLAY_NAME + " TEXT, "
                + CompetitorColumns.COMPETITOR_COUNTRY_CODE + " TEXT, "
                + CompetitorColumns.COMPETITOR_NATIONALITY + " TEXT, "
                + CompetitorColumns.COMPETITOR_SAIL_ID + " TEXT, "
                + CompetitorColumns.COMPETITOR_CHECKIN_DIGEST + " TEXT )");

            db.execSQL("CREATE TABLE " + Tables.EVENTS + " ( "
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + EventColumns.EVENT_ID + " TEXT, "
                + EventColumns.EVENT_NAME + " TEXTL, "
                + EventColumns.EVENT_DATE_START + " INTEGER, "
                + EventColumns.EVENT_DATE_END + " INTEGER, "
                + EventColumns.EVENT_SERVER + " TEXT, "
                + EventColumns.EVENT_IMAGE_URL + " TEXT, "
                + EventColumns.EVENT_CHECKIN_DIGEST + " TEXT )");

            db.execSQL(createMarkTable);
            db.execSQL(createBoatTable);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ExLog.i(mContext, TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
        db.beginTransaction();
        try {
            if (oldVersion < 2) {
                db.execSQL(createMarkTable);
                db.execSQL("ALTER TABLE " + Tables.CHECKIN_URIS + " ADD COLUMN " + Checkin.CHECKIN_TYPE + " INTEGER DEFAULT 0");
            }
            if (oldVersion < 3) {
                db.execSQL("ALTER TABLE " + Tables.LEADERBOARDS + " ADD COLUMN " + Leaderboard.LEADERBOARD_DISPLAY_NAME + " TEXT");
                db.execSQL("UPDATE " + Tables.LEADERBOARDS + " SET " + Leaderboard.LEADERBOARD_DISPLAY_NAME + " = " + Leaderboard.LEADERBOARD_NAME);
            }
            if (oldVersion < 4) {
                db.execSQL(createBoatTable);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

}
