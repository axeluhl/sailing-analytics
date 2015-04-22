package com.sap.sailing.android.tracking.app.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Competitor;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.CompetitorColumns;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Event;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.EventColumns;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Leaderboard;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.LeaderboardColumns;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.CheckinUriColumns;

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
        String LEADERBOARDS = "leaderboards";
        String CHECKIN_URIS = "checkin_uris";
        String EVENTS_JOIN_LEADERBOARDS_JOIN_COMPETITORS = Tables.LEADERBOARDS +
            " INNER JOIN " + Tables.EVENTS + " ON (" + Tables.LEADERBOARDS + "."
            + Leaderboard.LEADERBOARD_CHECKIN_DIGEST + " = " + Tables.EVENTS + "." + Event.EVENT_CHECKIN_DIGEST + ") " +
            " INNER JOIN " + Tables.COMPETITORS + " ON (" + Tables.LEADERBOARDS + "."
            + Leaderboard.LEADERBOARD_CHECKIN_DIGEST + " = " + Tables.COMPETITORS + "."
            + Competitor.COMPETITOR_CHECKIN_DIGEST + ") ";

        String LEADERBOARDS_JOIN_EVENTS =
            "events LEFT JOIN leaderboards ON " + Tables.EVENTS + "." + Event.EVENT_CHECKIN_DIGEST + " = "
                + Tables.LEADERBOARDS + "." + Leaderboard.LEADERBOARD_CHECKIN_DIGEST;
    }

    public AnalyticsDatabase(Context context) {
        super(context, DATABASE_NAME, null, CUR_DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + Tables.LEADERBOARDS + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LeaderboardColumns.LEADERBOARD_CHECKIN_DIGEST + " TEXT, " + LeaderboardColumns.LEADERBOARD_NAME
                + " TEXT );");

        db.execSQL(
            "CREATE TABLE " + Tables.CHECKIN_URIS + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CheckinUriColumns.CHECKIN_URI_CHECKIN_DIGEST + " TEXT, " + CheckinUriColumns.CHECKIN_URI_VALUE
                + " TEXT );");

        db.execSQL(
            "CREATE TABLE " + Tables.COMPETITORS + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CompetitorColumns.COMPETITOR_ID + " TEXT, " + CompetitorColumns.COMPETITOR_DISPLAY_NAME + " TEXT, "
                + CompetitorColumns.COMPETITOR_COUNTRY_CODE + " TEXT, " + CompetitorColumns.COMPETITOR_NATIONALITY
                + " TEXT, " + CompetitorColumns.COMPETITOR_SAIL_ID + " TEXT, "
                + CompetitorColumns.COMPETITOR_CHECKIN_DIGEST + " TEXT )");

        db.execSQL("CREATE TABLE " + Tables.EVENTS + " ( " + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + EventColumns.EVENT_ID + " TEXT, " + EventColumns.EVENT_NAME + " TEXTL, " + EventColumns.EVENT_DATE_START
            + " INTEGER, " + EventColumns.EVENT_DATE_END + " INTEGER, " + EventColumns.EVENT_SERVER + " TEXT, "
            + EventColumns.EVENT_IMAGE_URL + " TEXT, " + EventColumns.EVENT_CHECKIN_DIGEST + " TEXT )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ExLog.i(mContext, TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);

        int version = oldVersion;

        if (version != CUR_DATABASE_VERSION) {
            ExLog.i(mContext, TAG, "Upgrade unsuccessful - destroying old data during upgrade");

            db.execSQL("DROP TABLE IF EXISTS " + Tables.COMPETITORS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.EVENTS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.LEADERBOARDS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.CHECKIN_URIS);

            onCreate(db);
            version = CUR_DATABASE_VERSION;
        }
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }
}
