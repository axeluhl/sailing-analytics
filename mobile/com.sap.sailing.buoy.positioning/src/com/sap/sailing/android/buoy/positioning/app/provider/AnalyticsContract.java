package com.sap.sailing.android.buoy.positioning.app.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.sap.sailing.android.buoy.positioning.app.provider.AnalyticsDatabase.Tables;

public class AnalyticsContract {

    interface LeaderboardColumns {
        String LEADERBOARD_NAME = "leaderboard_name";
        /**
         * The values in this column either equal those of the {@link #LEADERBOARD_NAME} column in case no explicit
         * display name has been provided for the leaderboard, or they hold the explicit display name. Note that this
         * makes it impossible to tell whether the display name was inferred from the regular name or has been
         * explicitly provided.
         */
        String LEADERBOARD_DISPLAY_NAME = "leaderboard_display_name";
        String LEADERBOARD_SERVER_URL = "leaderboard_server_url";
        String LEADERBOARD_CHECKIN_DIGEST = "leaderboard_checkin_digest";
    }

    interface CheckinUriColumns {
        String CHECKIN_URI_VALUE = "uri_value";
        String CHECKIN_URI_CHECKIN_DIGEST = "uri_checkin_digest";
    }

    interface MarkColums {
        String MARK_ID = "mark_id";
        String MARK_NAME = "mark_name";
        String MARK_TYPE = "mark_type";
        String MARK_CLASS_NAME = "mark_class_name";
        String MARK_CHECKIN_DIGEST = "mark_checkin_digest";
    }

    interface MarkPingColumns {
        String MARK_ID = "mark_id";
        String MARK_PING_LONGITUDE = "mark_ping_longitude";
        String MARK_PING_LATITUDE = "mark_pink_latitude";
        String MARK_PING_ACCURACY = "mark_ping_accuracy";
        String MARK_PING_TIMESTAMP = "mark_ping_timestamp";
    }

    public static final String CONTENT_AUTHORITY = "com.sap.sailing.android.buoy.positioning.app.provider.db";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static class Leaderboard implements LeaderboardColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.LEADERBOARDS).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.sap_sailing_analytics_buoy.leaderboard";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.sap_sailing_analytics_buoy.leaderboard";
    }

    public static class CheckinUri implements CheckinUriColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.CHECKIN_URIS).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.sap_sailing_analytics_buoy.uri";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.sap_sailing_analytics_buoy.uri";
    }

    public static class Mark implements MarkColums, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.MARKS).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.sap_sailing_analytics_buoy.mark";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.sap_sailing_analytics_buoy.mark";
    }

    public static class MarkPing implements MarkPingColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.MARK_PINGS).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.sap_sailing_analytics_buoy.mark.ping";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.sap_sailing_analytics_buoy.mark.ping";
    }

    public static class LeaderboardsMarksJoined {
        public final static Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath("leaderboards_marks_joined")
                .build();
    }

    public static class MarksLeaderBoardsJoined {
        public final static Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath("marks_leaderboards_joined")
                .build();
    }

    private AnalyticsContract() {

    }
}
